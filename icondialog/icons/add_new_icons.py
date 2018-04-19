import os
import bisect
import xml.etree.ElementTree


# This script is used to automatically create XML entries for the icons in the \new folder
# All files except those beginning with "$" will be assigned a new unused ID
# New icons will be optimized with SVGO

current_path = os.getcwd()
new_folder = os.path.join(current_path, "new2")
output_xml = os.path.join(current_path, "new.xml")
icons_xml = os.path.join(current_path, "..\\src\\main\\res\\xml\\icons.xml")

svgo_precision = 2

id_list = None


def main():
    # Optimize SVG
    os.system("svgo -f {} -p {}".format(new_folder, svgo_precision))
    print()

    xml_icons = ""
    for path, subdirs, files in os.walk(new_folder):
        for name in files:
            if name.endswith(".svg"):
                replace = not name.startswith("$")
            
                icon_id = generate_id() if replace else int(name[1:-4])
                icon_path = os.path.join(path, name)
            
                # Create XML icon element
                labels = ",".join(name.split(".")[0].replace("_", "-").split("-"))
                if not replace: xml_icons += "(replaced) "
                xml_icons += "<icon id=\""
                xml_icons += str(icon_id) + "\" "
                if replace: xml_icons += "labels=\"" + labels + "\" "
                
                path_xml = xml.etree.ElementTree.parse(icon_path).findall(".//{http://www.w3.org/2000/svg}path") + \
                            xml.etree.ElementTree.parse(icon_path).findall(".//path")                
                if len(path_xml) != 1:
                    print("Icon \"{}\" has the wrong number of paths, ignored".format(name))
                    continue
                    
                xml_icons += "path=\"" + path_xml[0].attrib["d"] + "\"/>\n"
                    
                # Rename SVG file
                bisect.insort(id_list, icon_id)
                new_name = str(icon_id) + ".svg"
                new_path = os.path.join(path, new_name)
                os.replace(icon_path, new_path)
                print("Renamed \"{}\" to \"{}\"".format(name, new_name))

    icon_file = open(output_xml, "w")
    icon_file.write(xml_icons)
    icon_file.close()


def generate_id():
    global id_list
    if id_list is None:
        xml_file = xml.etree.ElementTree.parse(icons_xml).getroot()
        id_list = []
        for category_xml in xml_file.findall("category"):
            for icon_xml in category_xml.findall("icon"):
                bisect.insort(id_list, int(icon_xml.attrib["id"]))

    last_id = -1
    for id in id_list:
        if id != last_id + 1:
            return last_id + 1
        last_id = id


main()
    
print("DONE")
