package com.stormcloud.ide.api;

/*
 * #%L Stormcloud IDE - API - Web %% Copyright (C) 2012 - 2013 Stormcloud IDE %%
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>. #L%
 */
import com.stormcloud.ide.api.filesystem.IFilesystemManager;
import com.stormcloud.ide.api.filesystem.exception.FilesystemManagerException;
import com.stormcloud.ide.model.filesystem.*;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author martijn
 */
@Controller
@RequestMapping(value = "/filesystem")
public class FilesystemController extends BaseController {

    private Logger LOG = Logger.getLogger(getClass());
    @Autowired
    private IFilesystemManager manager;

    @RequestMapping(value = "/templates",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public Filesystem[] getTemplates()
            throws FilesystemManagerException {

        LOG.info("Get File Templates.");

        Filesystem[] response = new Filesystem[1];

        Filesystem filesystem = manager.getFileTemplates();

        response[0] = filesystem;

        return response;
    }

    @RequestMapping(value = "/bare",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public Filesystem[] bare()
            throws FilesystemManagerException {

        LOG.info("Get Bare Filesystem");

        Filesystem[] response = new Filesystem[1];

        Filesystem filesystem = manager.getFilesystem();

        response[0] = filesystem;

        return response;
    }

    @RequestMapping(value = "/folderpicker",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public Filesystem[] filePicker(
            @RequestParam String filePath)
            throws FilesystemManagerException {

        LOG.info("Folder Picking for " + filePath);

        Filesystem[] response = new Filesystem[1];

        Filesystem filesystem = manager.folderPicker(filePath);

        response[0] = filesystem;

        return response;
    }

    /**
     * Get the available maven projects in JSON format.
     *
     * The Filesystem[] return value is needed to be sure we feed the Dojo tree
     * model a JSON array.
     *
     * @return
     * @throws FilesystemManagerException
     */
    @RequestMapping(value = "/opened",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public Filesystem[] opened()
            throws FilesystemManagerException {

        LOG.info("Get Filesystem Opened.");

        Filesystem[] response = new Filesystem[1];

        Filesystem filesystem = manager.list(true);

        response[0] = filesystem;

        return response;
    }

    @RequestMapping(value = "/projects",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public Set<Item> getProjects()
            throws FilesystemManagerException {

        LOG.info("Get Projects");

        return manager.getProjects();
    }

    @RequestMapping(value = "/project",
    method = RequestMethod.POST,
    produces = "application/json")
    @ResponseBody
    public Set<Item> getProject(
            @RequestParam String filePath)
            throws FilesystemManagerException {

        LOG.info("Get Project " + filePath);

        return manager.getProject(filePath);
    }

    @RequestMapping(value = "/closed",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public Filesystem[] closed()
            throws FilesystemManagerException {

        LOG.info("Get Filesystem Closed.");

        Filesystem[] response = new Filesystem[1];

        Filesystem filesystem = manager.list(false);

        response[0] = filesystem;

        return response;
    }

    @RequestMapping(value = "/open",
    method = RequestMethod.POST)
    @ResponseBody
    public int open(
            @RequestParam String filePath)
            throws FilesystemManagerException {

        LOG.debug("Open Project[" + filePath + "]");

        return manager.open(filePath);
    }

    @RequestMapping(value = "/close",
    method = RequestMethod.POST)
    @ResponseBody
    public int close(
            @RequestParam String filePath)
            throws FilesystemManagerException {

        LOG.debug("Close Project[" + filePath + "]");

        return manager.close(filePath);
    }

    /**
     * Delete a file
     *
     * @param filePath
     * @return
     * @throws FilesystemManagerException
     */
    @RequestMapping(value = "/delete",
    method = RequestMethod.POST)
    @ResponseBody
    public int delete(
            @RequestParam String filePath)
            throws FilesystemManagerException {

        LOG.debug("Move file[" + filePath + "] to the Trash.");

        return manager.delete(filePath);
    }

    @RequestMapping(value = "/trash",
    method = RequestMethod.GET)
    @ResponseBody
    public Filesystem[] trash()
            throws FilesystemManagerException {

        LOG.debug("View Trash.");

        Filesystem[] response = new Filesystem[1];

        Filesystem filesystem = manager.viewTrash();

        response[0] = filesystem;

        return response;
    }

    @RequestMapping(value = "/emptyTrash",
    method = RequestMethod.POST)
    @ResponseBody
    public int emptyTrash()
            throws FilesystemManagerException {

        LOG.debug("Empty Trash.");

        return manager.emptyTrash();
    }

    @RequestMapping(value = "/hasTrash",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public int hasTrash()
            throws FilesystemManagerException {

        LOG.info("Check Trash");

        return manager.hasTrash();
    }

    @RequestMapping(value = "/move",
    method = RequestMethod.POST)
    @ResponseBody
    public int move(
            @RequestParam(value = "srcFilePath") String srcFilePath,
            @RequestParam(value = "destFilePath") String destFilePath)
            throws FilesystemManagerException {

        LOG.debug("Move src[" + srcFilePath + "] dest[" + destFilePath + "]");

        return manager.move(srcFilePath, destFilePath);
    }

    @RequestMapping(value = "/copy",
    method = RequestMethod.POST)
    @ResponseBody
    public int copy(
            @RequestParam(value = "srcFilePath") String srcFilePath,
            @RequestParam(value = "destFilePath") String destFilePath)
            throws FilesystemManagerException {

        LOG.debug("Copy src[" + srcFilePath + "] dest[" + destFilePath + "]");

        return manager.copy(srcFilePath, destFilePath);
    }

    /**
     * Save a file to disk
     *
     * @param save
     * @return
     * @throws FilesystemManagerException
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST,
    consumes = "application/json")
    @ResponseBody
    public int save(
            @RequestBody Save save)
            throws FilesystemManagerException {

        LOG.debug(
                "Saving filePath[" + save.getFilePath() + "], "
                + "contents[" + save.getContents() + "]");

        return manager.save(save);
    }

    @RequestMapping(value = "/find",
    method = RequestMethod.POST,
    consumes = "application/json",
    produces = "application/json")
    @ResponseBody
    public FindResult find(
            @RequestBody Find find)
            throws FilesystemManagerException {

        LOG.info("Find " + find.getContainingText());

        return manager.find(find);
    }

    /**
     * Create a file
     *
     * @param createFile
     * @return
     * @throws FilesystemManagerException
     */
    @RequestMapping(value = "/create",
    method = RequestMethod.POST,
    consumes = "application/json")
    @ResponseBody
    public int create(
            @RequestBody Create createFile)
            throws FilesystemManagerException {

        LOG.debug(
                "Creating filePath[" + createFile.getFilePath() + "], "
                + " template[" + createFile.getTemplate() + "]");

        return manager.create(createFile.getFilePath(), createFile.getTemplate());
    }

    /**
     * Get a file from disk
     *
     * @param filePath
     * @return
     * @throws FilesystemManagerException
     */
    @RequestMapping(value = "/get",
    method = RequestMethod.POST)
    @ResponseBody
    public String get(
            @RequestParam(value = "filePath", required = true) String filePath)
            throws FilesystemManagerException {

        LOG.debug("Get filePath[" + filePath + "]");

        String contents = manager.get(filePath);

        return contents;
    }

    @RequestMapping(value = "/getBinary",
    method = RequestMethod.POST)
    @ResponseBody
    public String getBinary(
            @RequestParam(value = "filePath", required = true) String filePath)
            throws FilesystemManagerException {

        LOG.debug("Get Binary filePath[" + filePath + "]");

        String contents = manager.getBinary(filePath);

        return contents;
    }
}
