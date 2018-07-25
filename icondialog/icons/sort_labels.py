import os
import codecs
from xml.etree import ElementTree

# This script sort a XML label file alphabetically

current_path = os.getcwd()
labels_file = os.path.join(current_path, "..\\src\\main\\res\\xml-pt\\icd_labels.xml")
        
class Label:

    def __init__(self, name, body):
        self.name = name
        self.body = body
        
    def __lt__(self, label):
        return self.name < label.name
        
    def __eq__(self, label):
        return self.name == label.name


def main():
    # List all icons and all labels
    label_root_xml = ElementTree.parse(labels_file).getroot()
    label_list = []
    for label_xml in label_root_xml.findall("label"):
        name = label_xml.get("name")
        body = None
        aliases = label_xml.findall("alias")
        if len(aliases) == 0:
            body = label_xml.text
        else:
            body = "\n"
            for alias_xml in aliases:
                body += "        <alias>" + alias_xml.text + "</alias>\n"
            body += "    "
            
        label_list.append(Label(name, body))

    label_list.sort()
    
    xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<list>\n\n"
    for label in label_list:
        xml += "    <label name=\"" + label.name + "\">" + label.body + "</label>\n"
    xml += "\n</list>"
   
    icon_file = codecs.open(labels_file, "w", "utf-8")
    icon_file.write(xml)
    icon_file.close()

main()

print("DONE")
