"""Static content checks; these do not replace Android compilation or device tests."""
import json
import math
from pathlib import Path
import xml.etree.ElementTree as ET
root = Path(__file__).resolve().parents[1]
recipes = json.loads((root/'app/src/main/assets/recipes.json').read_text(encoding='utf-8'))
ids = set()
types = {}
for recipe in recipes:
    assert recipe['id'] not in ids
    ids.add(recipe['id'])
    assert recipe['title'].strip() and recipe['steps']
    assert 0 < recipe['minutes'] <= 180 and recipe['servings'] > 0
    assert recipe['type'] in ('peixe', 'carne', 'vegetariana')
    types[recipe['type']] = types.get(recipe['type'], 0) + 1
    for ingredient in recipe['ingredients']:
        assert ingredient['name'].strip() and ingredient['category'].strip()
        assert math.isfinite(ingredient['qty']) and ingredient['qty'] > 0
        assert ingredient['unit'] in ('g', 'ml', 'un')
    if recipe['type'] == 'peixe':
        assert any('63 °C' in step for step in recipe['steps'])
assert all(types.get(t, 0) >= 7 for t in ('peixe', 'carne', 'vegetariana'))
for xml in (root/'app/src/main').rglob('*.xml'):
    ET.parse(xml)
manifest = ET.parse(root/'app/src/main/AndroidManifest.xml')
ns = '{http://schemas.android.com/apk/res/android}'
assert manifest.getroot().find('application').get(ns+'allowBackup') == 'false'
assert manifest.getroot().find('application').get(ns+'usesCleartextTraffic') == 'false'
print(f'OK: {len(recipes)} receitas, IDs únicos, ingredientes normalizados, distribuição e XML.')
