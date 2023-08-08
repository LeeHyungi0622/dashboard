package io.dtonic.dhubingestmodule.controller.web;

import io.dtonic.dhubingestmodule.security.service.IngestManagerSecuritySVC;
import io.dtonic.dhubingestmodule.security.vo.UserVO;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller
public class IngestManagerViewController implements ErrorController {

    @Autowired
    private IngestManagerSecuritySVC ingestManagerSVC;

    /**
     * When the page is refreshed In spa development,
     * refreshing the screen means server-side rendering,
     * so you need to convert the screen to index.html.
     *
     * @return String html page name
     * @throws IOException Throw an exception when an IO error occurs.
     */

    @GetMapping({ "/", "/error" })
    public String redirectRoot(
        HttpSession session,
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws IOException {
        return "index.html";
    }
    
    // @GetMapping("/list")
    // public String pipelineListView(
    //     HttpSession session,
    //     HttpServletRequest request,
    //     HttpServletResponse response
    // )
    //     throws IOException {
    //     return "index.html";
    // }
    @GetMapping("/pipelineRegister")
    public String pipelineRegisterView(
        HttpSession session,
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws IOException {
        return "index.html";
    }
    @GetMapping("/pipelineRegister/new")
    public String pipelineNewRegisterView(
        HttpSession session,
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws IOException {
        return "index.html";
    }

    @GetMapping("/pipelineUpdate")
    public String pipelineUpdateByIdView(
        HttpSession session,
        HttpServletRequest request,
        HttpServletResponse response
    )
            throws IOException {
        return "index.html";
    }
    
    @GetMapping("/redirectNiFi")
    public String redirectNiFi(
        HttpSession session,
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws IOException {
        return "index.html";
    }

    /**
     * When requesting accesstoken url, it responds by obtaining a token.
     *
     * @throws IOException Throw an exception when an error occurs.
     */
    @GetMapping("/accesstoken")
    public @ResponseBody void getAccessToken(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws Exception {
        ingestManagerSVC.getAccessToken(request, response);

        String contextPath = request.getContextPath();
        response.sendRedirect(contextPath + "/");
    }

    @GetMapping("/security")
    public ResponseEntity<Boolean> getSecurityInfo(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws Exception {
        return ingestManagerSVC.getSecurityInfo();
    }

    /**
     * Responds to user information when requesting user url.
     *
     * @return User information
     * @throws Exception Throw an exception when an error occurs.
     */
    @GetMapping("/user")
    public ResponseEntity<UserVO> getUser(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        return ingestManagerSVC.getUser(request);
    }

    /**
     * Responds to user ID when requesting user url.
     *
     * @return User ID
     * @throws Exception Throw an exception when an error occurs.
     */
    @GetMapping("/userId")
    public ResponseEntity<String> getUserId(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws Exception {
        return ingestManagerSVC.getUserId(request);
    }

    /**
     * Logout processing when requesting logout url.
     *
     * @return Http status
     * @throws Exception Throw an exception when an IO error occurs.
     */
    @GetMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        ingestManagerSVC.logout(request, response, null);

        return ResponseEntity.ok().build();
    }

    /**
     * Response error page
     */
    public String getErrorPath() {
        return "/error";
    }
}
