import sys
import os
import bisect
import xml.etree.ElementTree

# This script checks label usage in icons to determine which icons has less labels
# and to identify labels that could be removed

current_path = os.getcwd()
icons_file = os.path.join(current_path, "..\\src\\main\\res\\xml\\icons.xml")

class Icon:
    
    def __init__(self, id, labels):
        self.id = id
        self.labels = labels
        
    def __lt__(self, icon):
        return self.id < icon.id
        
class Label:

    def __init__(self, name):
        self.name = name
        self.uses = 1
        
    def __lt__(self, label):
        return self.name < label.name
        
    def __eq__(self, label):
        return self.name == label.name

def list_index_of(list, element):
    pos = bisect.bisect_left(list, element)
    if pos != len(list) and list[pos] == element:
        return pos
    else:
        return -1


def main():
    # List all icons and all labels
    icon_root_xml = xml.etree.ElementTree.parse(icons_file).getroot()
    icon_list = []
    label_list = []
    for categoryXml in icon_root_xml.findall("category"):
        for icon_xml in categoryXml.findall("icon"):
            id = int(icon_xml.get("id"))
            labels_raw = icon_xml.get("labels")
            labels_str = labels_raw.split(",")

            icon_labels = []
            for label_str in labels_str:
                if not label_str.startswith("_"):
                    label = Label(label_str)
                    index = list_index_of(label_list, label)
                    if index == -1:
                        bisect.insort(label_list, label)
                    else:
                        label = label_list[index]
                        label.uses += 1
                        
                    icon_labels.append(label)

            icon_list.append(Icon(id, icon_labels))

    icon_list.sort()
    
    # Print labels by uses
    print("Labels by uses:")
    labels_by_uses = sorted(label_list, key=lambda label: label.uses)
    same_uses = []
    last_uses = labels_by_uses[0].uses
    for label in labels_by_uses:
        if label.uses != last_uses:
            print("{} uses ({}) -> {}".format(last_uses, len(same_uses), ", ".join(same_uses)))
            last_uses = label.uses
            same_uses = []
            
        same_uses.append(label.name)
            
    print("{} uses ({}) -> {}".format(last_uses, len(same_uses), ", ".join(same_uses)))
    
    # Print icons by number of labels
    print("\nIcons by number of labels:")
    icons_by_labels = sorted(icon_list, key=lambda icon: len(icon.labels))
    same_count = []
    last_count = len(icons_by_labels[0].labels)
    for icon in icons_by_labels:
        if len(icon.labels) != last_count:
            print("{} labels ({}) -> {}".format(last_count, len(same_count), ", ".join(same_count)))
            last_count = len(icon.labels)
            same_count = []
            
        same_count.append(str(icon.id))
            
    print("{} labels ({}) -> {}".format(last_count, len(same_count), ", ".join(same_count)))
    

main()

print("DONE")
