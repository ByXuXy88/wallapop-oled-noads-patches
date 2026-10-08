"""Structural verification after an actual FULL-mode APK patch/rebuild."""
import sys,zipfile,json,hashlib
from pathlib import Path
from loguru import logger
logger.remove()
from androguard.core.apk import APK
from androguard.core.dex import DEX
original,patched=map(Path,sys.argv[1:3]);p=APK(str(patched))
assert (p.get_package(),p.get_androidversion_name(),p.get_androidversion_code())==('com.wallapop','1.334.0','10141414')
assert p.is_signed_v2()
targets={('Lcom/wallapop/ads/featureflags/domain/usecase/ShouldShowAdsCommand;','invoke'),('Lcom/wallapop/ads/featureflags/data/AdsFeatureFlagsDataSourceImpl;','getShouldShowAds')}
found=set();surface_ok=theme_ok=False
with zipfile.ZipFile(patched) as z:
    for name in z.namelist():
        if not name.endswith('.dex'):continue
        d=DEX(z.read(name))
        for cl in d.get_classes():
            if cl.get_name() not in {'Lwkh;','Loxu;'}|{x[0] for x in targets}:continue
            for m in cl.get_methods():
                ins=list(m.get_instructions());key=(cl.get_name(),m.get_name())
                if key in targets and m.get_descriptor()=='()Z':
                    assert ins[0].get_name()=='const/4' and ins[0].get_output()=='v0, 0'
                    assert ins[1].get_name()=='return' and ins[1].get_output()=='v0';found.add(key)
                if key==('Lwkh;','<init>'):
                    tail=ins[-6:];assert tail[0].get_name()=='if-eqz' and tail[0].get_output().startswith('v1,')
                    assert tail[1].get_name()=='const-wide' and '-72057594037927936' in tail[1].get_output()
                    assert [i.get_output().split('->')[1].split()[0] for i in tail[2:5]]==['b','c','d']
                    assert all(i.get_output().startswith('v1, v0,') for i in tail[2:5]);assert tail[-1].get_name()=='return-void';surface_ok=True
                if key==('Loxu;','a') and m.get_descriptor()=='(Lxjh; Lcc6; Lbf6; I)V':
                    ix=next(i for i,x in enumerate(ins) if x.get_name()=='sget-object' and 'Leph;->a Lwkh;' in x.get_output())
                    assert ins[ix-2].get_name()=='invoke-static' and 'v3, Lww00;->b(Lbf6;)Z' in ins[ix-2].get_output()
                    assert ins[ix-1].get_name()=='move-result' and ins[ix-1].get_output()=='v6';theme_ok=True
assert found==targets and surface_ok and theme_ok
r=p.get_android_resources();rid=r.get_res_id_by_key('com.wallapop','style','BaseAppTheme')
assert all(r.get_resource_xml_name(e.item.id_parent)=='@com.wallapop:style/Theme.AppCompat.DayNight.NoActionBar' for _,e in r.get_res_configs(rid))
black_night=0
for i in range(12):
    rid=r.get_res_id_by_key('com.wallapop','color',f'wallapop_oled_role_{i}');assert rid
    entries=r.get_resolved_res_configs(rid)
    if any(c.get_qualifier()=='night' and v=='#FF000000' for c,v in entries):black_night+=1
assert black_night>=6
with zipfile.ZipFile(original) as a,zipfile.ZipFile(patched) as b:
    libs={n:a.read(n) for n in a.namelist() if n.startswith('lib/') and n.endswith('.so')}
    assert {n for n in b.namelist() if n.startswith('lib/') and n.endswith('.so')}==set(libs)
    assert all(b.read(n)==data for n,data in libs.items())
report={'package':'com.wallapop','version':'1.334.0','advertising_gates':'verified in rebuilt DEX','compose_black_surfaces':'verified in rebuilt DEX','system_dark_mode_selection':'verified in rebuilt DEX','daynight_parent':'verified in rebuilt resource table','black_night_colour_resources':black_night,'native_libraries_present_and_preserved':len(libs),'required_split_types':p.get_android_manifest_xml().get('{http://schemas.android.com/apk/res/android}requiredSplitTypes'),'apk_v2_signature_present':True,'sha256':hashlib.sha256(patched.read_bytes()).hexdigest(),'runtime':'Not installed or visually tested on a phone'}
print(json.dumps(report,indent=2))
