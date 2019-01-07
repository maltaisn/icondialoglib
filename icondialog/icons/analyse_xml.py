import bisect
import os
import xml.etree.ElementTree
import sys

# This script checks for errors in XML files
# -> icons.xml
#    - Duplicate icon IDs
#    - Unused and missing SVG drawable files (in icons/svg)
#    - Duplicate labels used by one icon
#    - Empty label typo, i.e: ", / ,, / ,"
#    - Icons with missing attributes (labels or path)
#
# -> labels.xml
#    - Unused and missing labels
#    - Duplicate label names
#    - Label and aliases with unescaped quotes (" or ') in value
#    - Labels and aliases that have the same value (and could be a reference instead)
#    - References that don't point to a valid label
#    - References to another reference
#
# Three levels of messages:
#    - INFO: only to help improving the files
#    - WARNING: should be fixed, won't cause crash
#    - ERROR: will cause crash, must be fixed
#
# IMPORTANT: Script made hastily, may not work as expected

os.chdir(sys.path[0])
current_path = os.getcwd()
default_label_path = os.path.join(current_path, "..\\src\\main\\res\\xml") 
default_label_file = "icd_labels.xml"
default_lang = ["", "-fr", "-pt", "-de"]

# Settings to analyse custom icons and labels
# is_extra_icons = True
# icons_file = os.path.join(current_path, "..\\..\\app\\src\\main\\res\\xml\\icons.xml")
# label_file = "labels.xml"
# label_path = os.path.join(current_path, "..\\..\\app\\src\\main\\res\\xml")
# label_lang = ["", "-fr"]

# Settings to analyse default icons and labels
is_extra_icons = False
icons_file = os.path.join(current_path, "..\\src\\main\\res\\xml\\icd_icons.xml")
label_file = default_label_file
label_path = default_label_path
label_lang = default_lang
svg_folder = os.path.join(current_path, "svg")

class Icon:
    
    def __init__(self, id, category, labels):
        self.id = id
        self.category = category
        self.labels = labels
        
    def __lt__(self, icon):
        return self.id < icon.id
        
class Label:
    
    def __init__(self, name, value=None, aliases=None):
        self.name = name
        self.value = value
        self.aliases = aliases
        
    def __lt__(self, label):
        return self.name < label.name
        
    def __eq__(self, label):
        return self.name == label.name
        
class LabelRef:

    TYPE_LABEL = 0
    TYPE_ICDLABEL = 1

    def __init__(self, name, value):
        self.name = name
        sep_index = value.find("/")+1
        self.dest = value[sep_index:]
        self.type = LabelRef.TYPE_LABEL if sep_index == 7 else LabelRef.TYPE_ICDLABEL
        
class LabelValue:

    def __init__(self, value):
        self.value = value
        self.isRef = value[0] == "@"


def list_index_of(list, element):
    pos = bisect.bisect_left(list, element)
    if pos != len(list) and list[pos] == element:
        return pos
    else:
        return -1


def main():
    # List all icons and all labels
    print("-> Icon file")
    icon_root_xml = xml.etree.ElementTree.parse(icons_file).getroot()
    icon_list = []
    label_list = []
    duplicate_labels = []
    empty_label_icons = []
    missing_attributes = []
    for categoryXml in icon_root_xml.findall("category"):
        category_id = int(categoryXml.attrib["id"])
        for icon_xml in categoryXml.findall("icon"):
            id = int(icon_xml.attrib["id"])
            try:
                labels_raw = icon_xml.attrib["labels"]
                path_raw = icon_xml.attrib["path"]
            except KeyError:
                missing_attributes.append(id)
            else:
                labels_str = labels_raw.split(",")

                icon_labels = []
                for label_str in labels_str:
                    if len(label_str) == 0:
                        if list_index_of(empty_label_icons, id) == -1:
                            bisect.insort(empty_label_icons, id)
                    elif list_index_of(icon_labels, label_str) != -1:
                        duplicate_labels.append(str(id))
                    else:
                        bisect.insort(icon_labels, label_str)
                        if not label_str.startswith("_"):
                            index = list_index_of(label_list, label_str)
                            if index == -1:
                                bisect.insort(label_list, label_str)

                icon_list.append(Icon(id, category_id, icon_labels))

    icon_list.sort()
    
    # Print duplicate icon IDs
    id_list = [icon.id for icon in icon_list]
    dup_icon_ids = []
    for i in range(len(id_list)-1, 0, -1):
        if id_list[i] == id_list[i-1]:
            dup_icon_ids.append(str(id_list[i]))
            del id_list[i]
    if len(dup_icon_ids) > 0:
        text = ", ".join(dup_icon_ids)
        print("     ERROR: {} duplicate icon IDs: ".format(len(dup_icon_ids), text))
        
    if not is_extra_icons:
        # Print missing icon SVG
        found_svg = [False] * len(id_list)
        unused_svg = []
        for file in os.listdir(svg_folder):
            try:
                id = int(file[:-4])
                index = list_index_of(id_list, id)
                if index == -1:
                    unused_svg.append(id)
                else:
                    found_svg[index] = True
            except ValueError:
                continue
                
        # Print unused SVG files
        if len(unused_svg) > 0:
            text = ", ".join([str(id) for id in unused_svg])
            print("     INFO: {} unused SVG files: {}".format(len(unused_svg), text))
                
        missing_svg = []
        for i, found in enumerate(found_svg):
            if not found:
                missing_svg.append(id_list[i])
        
        # Print unused SVG files
        if len(missing_svg) > 0:
            text = ", ".join([str(id) for id in missing_svg])
            print("     INFO: {} missing SVG files: {}".format(len(missing_svg), text))
            
        # Print icons with missing attribute
        if len(missing_attributes) > 0:
            text = ", ".join([str(id) for id in missing_attributes])
            print("     ERROR: {} icons with missing attributes: {}".format(len(missing_attributes), text))
        
    # Print icons with empty label (", or ,, or ",)
    if len(empty_label_icons) > 0:
        text = ", ".join([str(id) for id in empty_label_icons])
        print("     ERROR: {} icons with empty labels: ".format(len(empty_label_icons), text))
    
    # Print duplicate labels
    if len(duplicate_labels) > 0:
        text = ", ".join(duplicate_labels)
        print("     ERROR: {} icons with duplicate labels: ".format(len(duplicate_labels), text))
        
    print("     DONE")
    
    # Check for missing and unused labels
    for lang in label_lang:
        # If analysing extra icons, add default labels
        default_lang_labels = []
        if is_extra_icons and lang in default_lang:
            path = os.path.join(default_label_path + lang, default_label_file)
            defaut_label_root_xml = xml.etree.ElementTree.parse(path).getroot()
            for label_xml in defaut_label_root_xml.findall("label"):
                name = label_xml.get("name")
                label = Label(name)

                bisect.insort(default_lang_labels, label)
                
                children = label_xml.findall("alias")
                if len(children) == 0:
                    text = label_xml.text
                    label.value = LabelValue(text)
                else:
                    label.aliases = []
                    for i, alias_xml in enumerate(children):
                        text = alias_xml.text
                        full_name = name + "$" + str(i)
                        label.aliases.append(LabelValue(text))
    
        path = os.path.join(label_path + lang, label_file)
        label_root_xml = xml.etree.ElementTree.parse(path).getroot()
        lang_labels = []
        unused = []
        found_labels = [False] * len(label_list)
        label_refs = []
        duplicate_labels = []
        unescaped_value = []
        for label_xml in label_root_xml.findall("label"):
            name = label_xml.get("name")
            label = Label(name)
            if list_index_of(lang_labels, label) != -1:
                duplicate_labels.append(name)
            else:
                bisect.insort(lang_labels, label)
                index = list_index_of(label_list, name)
                index_in_def = list_index_of(default_lang_labels, Label(name)) if is_extra_icons else 0
                
                if index == -1 and not (is_extra_icons and index_in_def != -1):
                    # Unused label
                    unused.append(name)
                else:
                    found_labels[index] = True
                
                children = label_xml.findall("alias")
                if len(children) == 0:
                    text = label_xml.text
                    label.value = LabelValue(text)
                    if text[0] == "@":
                        label_refs.append(LabelRef(name, text))
                    if '"' in text or "'" in text:
                        unescaped_value.append(name)
                else:
                    label.aliases = []
                    for i, alias_xml in enumerate(children):
                        text = alias_xml.text
                        full_name = name + "$" + str(i)
                        label.aliases.append(LabelValue(text))
                        if text[0] == "@":
                            label_refs.append(LabelRef(full_name, text))
                        if '"' in text or "'" in text:
                            unescaped_value.append(full_name)
                     
        print("-> Label file: \\xml{}\\{}".format(lang, label_file))
        
        # Print unused labels
        if len(unused) > 0:
            text = ", ".join(unused)
            print("     WARNING: {} unused labels: {}".format(len(unused), text))
        
        # Print missing labels
        missing = []
        for i, found in enumerate(found_labels):
            if not found:
                name = label_list[i]
                if is_extra_icons and list_index_of(default_lang_labels, Label(name)) != -1:
                    continue
                missing.append(name)
        if len(missing) > 0:
            text = ", ".join(missing)
            print("     WARNING: {} missing labels: {}".format(len(missing), text))
            
        # Print duplicate labels
        if len(duplicate_labels) > 0:
            text = ", ".join(duplicate_labels)
            print("     ERROR: {} duplicate labels: {}".format(len(duplicate_labels), text))
            
        # Print labels with unescaped chars
        if len(unescaped_value) > 0:
            text = ", ".join(unescaped_value)
            print("     ERROR: {} labels with unescaped quotes: {}".format(len(unescaped_value), text))
            
        # Print labels or aliases with the same value
        label_values = {}
        for label in lang_labels:
            if label.value is None:
                for i, alias in enumerate(label.aliases):
                    if not alias.isRef:
                        label_values.setdefault(alias.value, []).append(label.name + "$" + str(i))
            elif not label.value.isRef:
                label_values.setdefault(label.value.value, []).append(label.name)
        dup_label_val = [key for key, values in label_values.items() if len(values) > 1]
        if len(dup_label_val) > 0:
            text = ", ".join(["/".join(label_values[value]) for value in dup_label_val])
            print("     INFO: {} label sets with same value: {}".format(len(dup_label_val), text))

        # Print unknown and wrong references
        unkn_ref = []
        ref_to_ref = []
        for ref in label_refs:
            ref_alias_sign = ref.dest.find("$")
            ref_alias = -1
            ref_name = ref.dest
            if ref_alias_sign != -1:
                # Ref is ref to alias
                ref_alias = int(ref.dest[ref_alias_sign+1:])
                ref_name = ref.dest[:ref_alias_sign]
                
            index_label = Label(ref_name)
            index = list_index_of(lang_labels, index_label)
            if index == -1:
                # Ref to unkn label name
                if not (is_extra_icons and ref.type == LabelRef.TYPE_ICDLABEL \
                        and list_index_of(default_lang_labels, index_label) != -1):
                    unkn_ref.append(ref.name + " -> " + ref.dest)
            else:
                ref_dest = lang_labels[index]
                if ref_alias_sign == -1:
                    if ref_dest.value != None and ref_dest.value.isRef and ref.type != LabelRef.TYPE_ICDLABEL:
                        # Ref to label with ref
                        ref_to_ref.append(ref.name + " -> " + ref.dest)
                else:
                    if ref_dest.aliases is None or len(ref_dest.aliases) <= ref_alias:
                        # Ref to unkn alias
                        unkn_ref.append(ref.name + " -> " + ref.dest)
                    elif ref_dest.aliases[ref_alias].isRef and ref.type != LabelRef.TYPE_ICDLABEL:
                        # Ref to alias with ref
                        ref_to_ref.append(ref.name + " -> " + ref.dest)
                        
        if len(unkn_ref) > 0:
            text = ", ".join(unkn_ref)
            print("     ERROR: {} unknown label references: {}".format(len(unkn_ref), text))
            
        if len(ref_to_ref) > 0:
            text = ", ".join(ref_to_ref)
            print("     ERROR: {} references to another reference: {}".format(len(ref_to_ref), text))
            
        print("     DONE")


main()
