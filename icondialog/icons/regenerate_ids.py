import os
import xml.etree.ElementTree

# This script sort icons by category, then by labels and then by ID
# Icon ID is changed to the icon's position in the sorted list
# This script shouldn't be ever run again I guess

current_path = os.getcwd()
icons_file = os.path.join(current_path, "icons.xml")
output_file = os.path.join(current_path, "output-icons.xml")
svg_folder = os.path.join(current_path, "svg")


class Icon:
    
    def __init__(self, id, category, labels, path):
        self.id = id
        self.category = category
        self.labels = labels
        self.path = path
        
    def __lt__(self, icon):
        if self.category == icon.category:
            len1 = len(self.labels)
            len2 = len(icon.labels)
            for i in range(min(len1, len2)):
                label1 = self.labels[i]
                label2 = icon.labels[i]
                if label1 != label2:
                    return label1 < label2
                    
            if len1 != len2:
                return len1 < len2
            else:
                return self.id < icon.id
        else:
            return self.category < icon.category
        
class Category:
    
    def __init__(self, id, name):
        self.id = id
        self.name = name
        
    def __lt__(self, catg):
        return self.id < catg.id

def main():
    print("Sorting icons")
    icon_root_xml = xml.etree.ElementTree.parse(icons_file).getroot()
    icon_list = []
    max_id = 0
    for categoryXml in icon_root_xml.findall("category"):
        category = Category(int(categoryXml.attrib["id"]), categoryXml.attrib["name"])
        for icon_xml in categoryXml.findall("icon"):
            id = int(icon_xml.attrib["id"])
            labels = icon_xml.attrib["labels"].split(",")
            path = icon_xml.attrib["path"]
            icon_list.append(Icon(id, category, labels, path))
            if id > max_id: max_id = id
            

    icon_list.sort()
    
    # Recreating XML
    print("Generating XML")
    xml_str = "<list>\n"
    last_catg = -1
    for i, icon in enumerate(icon_list):
        if icon.category.id != last_catg:
            if last_catg != -1: xml_str += "    </category>\n\n"
            xml_str += "    <category id=\"{}\" name=\"{}\">\n".format(icon.category.id, icon.category.name)
            last_catg = icon.category.id
        
        xml_str += "        <icon id=\"{}\" labels=\"{}\" path=\"{}\"/>\n".format(i, ",".join(icon.labels), icon.path)
        
    xml_str += "    </category>\n</list>" 
    
    output = open(output_file, "w")
    output.write(xml_str)
    output.close()
       
    id_rename = [None] * (max_id + 1)
    for i, icon in enumerate(icon_list):
        id_rename[icon.id] = i

    # Rename svg files
    print("Renaming SVG files")
    for file in os.listdir(svg_folder):
        try:
            old_id = int(file[:-4])
            old = os.path.join(svg_folder, file)
            new = os.path.join(svg_folder, "_" + str(id_rename[old_id]) + ".svg")
            os.rename(old, new)
        except ValueError:
            continue
            
    # Remove the temp "_" before names
    print("Remove temp prefix")
    for file in os.listdir(svg_folder):
        try:
            old = os.path.join(svg_folder, file)
            new = os.path.join(svg_folder, file[1:])
            os.rename(old, new)
        except ValueError:
            continue
            
    print("DONE")


main()
