package com.stormcloud.ide.api;

import com.stormcloud.ide.api.core.entity.ArchetypeCatalog;
import com.stormcloud.ide.api.filesystem.exception.FilesystemManagerException;
import com.stormcloud.ide.api.maven.IMavenManager;
import com.stormcloud.ide.api.maven.exception.MavenManagerException;
import com.stormcloud.ide.model.maven.Project;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author martijn
 */
@Controller
@RequestMapping(value = "/maven")
public class MavenController {

    private Logger LOG = Logger.getLogger(getClass());
    @Autowired
    private IMavenManager manager;

    @RequestMapping(value = "/archetypes",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public ArchetypeCatalog[] getCatalog()
            throws MavenManagerException {

        ArchetypeCatalog[] archetypes = manager.getCatalog();

        return archetypes;
    }

    /**
     * Create new maven project and return the available maven projects in XML
     * format.
     *
     * @param project
     * @return
     * @throws MavenManagerException
     * @throws FilesystemManagerException
     */
    @RequestMapping(value = "/create",
    method = RequestMethod.POST,
    consumes = "application/json")
    @ResponseBody
    public int createProject(
            @RequestBody Project project)
            throws MavenManagerException, FilesystemManagerException {

        LOG.debug(
                "Create project[" + project + "]");

        return manager.createProject(project);
    }

    /**
     * Compile a project
     *
     * @param commands
     * @param filePath
     * @return
     * @throws MavenManagerException
     */
    @RequestMapping(value = "/execute",
    method = RequestMethod.POST)
    @ResponseBody
    public int execute(
            @RequestParam(value = "commands", required = true) String commands,
            @RequestParam(value = "filePath", required = true) String filePath)
            throws MavenManagerException {

        LOG.debug(
                "Compiling filePath[" + filePath + "]");

        return manager.execute(commands, filePath);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.METHOD_FAILURE)
    @ResponseBody
    public String handleServerErrors(Exception ex) {
        return ex.getMessage();
    }
}
