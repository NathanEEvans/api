package com.stormcloud.ide.api.filesystem;

import com.stormcloud.ide.model.filesystem.Item;
import com.stormcloud.ide.model.filesystem.ItemType;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author martijn
 */
public class Test {

    public static void main(String[] args) {

        new Test().run();

    }

    public void run() {

        Set<Item> result = new LinkedHashSet<Item>(0);

        Set<String> packages = new LinkedHashSet<String>(0);

        File root = new File("/Users/martijn/Projects/stormcloud/sources/stormcloud-ide/api/core/src/main/java");

        Collection<File> files = FileUtils.listFiles(root, null, true);

        for (File file : files) {

            // capture the package
            packages.add(file.getParent());

            // construct the file
            Item item = new Item();
            item.setId(file.getAbsolutePath());
            item.setParent(file.getParent());
            item.setLabel(file.getName());
            item.setDirectory(file.isDirectory());
            item.setStyle(null);


        }

        Set<String> emptyPackages = new LinkedHashSet<String>(0);

        // check for empty dirs
        getEmptyPackages(root, emptyPackages);

        for (String string : emptyPackages) {

            System.out.println(string);
        }

        for (String itm : packages) {

            Item item = new Item();
            item.setId(itm);
            item.setParent(root.getAbsolutePath());
            item.setDirectory(true);
            String path = itm.replaceFirst(root.getAbsolutePath(), "");
            String label = path.replaceFirst("/", "").replaceAll("/", ".");
            item.setLabel(label);
            item.setType(ItemType.FOLDER);
            item.setStyle("package");

            //System.out.println(item.getId() + " " + item.getLabel());
        }


    }

    public void getEmptyPackages(File aFile, Set<String> emptyPackages) {

        if (aFile.isDirectory()) {

            File[] listOfFiles = aFile.listFiles();

            if (listOfFiles.length == 0) {

                emptyPackages.add(aFile.getAbsolutePath());

            } else {


                for (int i = 0; i < listOfFiles.length; i++) {


                    getEmptyPackages(listOfFiles[i], emptyPackages);
                }
            }

        }

    }
}
