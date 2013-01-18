<%--
  #%L
  Stormcloud IDE - API - Web
  %%
  Copyright (C) 2012 - 2013 Stormcloud IDE
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as
  published by the Free Software Foundation, either version 3 of the 
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public 
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/gpl-3.0.html>.
  #L%
  --%>
<%-- 
    Document   : login
    Created on : Nov 30, 2012, 11:35:48 AM
    Author     : martijn
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Stormcloud IDE</title>

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <link rel="stylesheet" href="css/claro/document.css">
        <link rel="stylesheet" href="css/claro/claro.css">
        <link rel="stylesheet" href="css/login.css">

        <script type="text/javascript">dojoConfig = {parseOnLoad: true}</script>
        <script type="text/javascript" src="js/dojo/dojo.js"></script>
        <script type="text/javascript" src="js/stormcloud/Login.js"></script>

    </head>

    <body class="claro">

        <div data-dojo-type="dojox/widget/DialogSimple" data-dojo-id="loginDialog" data-dojo-props="closeble:false, href: 'dialogs/LoginDialog.html'" title="Your Credentials Please ..."></div>

        <div data-dojo-type="dijit/layout/BorderContainer" data-dojo-props="design:'sidebar', gutters:true" id="borderContainer">


            <div data-dojo-type="dijit/layout/ContentPane" id="welcomeBanner" data-dojo-props="region:'top'" style="border:0px; height: 100px;">

                <span style="font-size: 32px; color: #ffffff"><b>Stormcloud IDE</b></span><span id="betaTag">Beta</span>
                <br>
                <span style="font-size:11px; color: #b5bcc7"><b>Unleash the power of Integration in the Cloud</b></span>
                <div style="text-align: right;">
                    <span style="font-size: 38px; color: #ffffff"><b>Login</b></span>
                </div>
            </div>


            <div data-dojo-type="dijit/layout/ContentPane" data-dojo-props="region:'left'" style="width: 20%;">

                News... or twitter... etc

            </div>

            <div data-dojo-type="dijit/layout/ContentPane" data-dojo-props="region:'center'" style="width: 80%">


                <div data-dojo-type="dijit/layout/ContentPane" data-dojo-props="region:'top'" style="height: 120px; text-align: center;">

                    <table style="width: 100%;">
                        <tr>
                            <td style="width: 10%;">
                                <label for="name" style="font-size: 12px;">Username</label>
                            </td>
                            <td style="width: 90%; text-align: left;">
                                <input id="username" data-dojo-type="dijit/form/TextBox" style=" width: 250px; font-size: 16px;">
                            </td>
                        </tr>

                        <tr>
                            <td style="width: 10%;">
                                <label for="password" style="font-size: 12px;">Password</label>
                            </td>
                            <td style="width: 90%; text-align: left;">
                                <input type="password" id="password" data-dojo-type="dijit/form/TextBox" style="width: 250px; font-size: 16px;">
                            </td>
                        </tr>
                        <tr>
                            <td></td>
                            <td style="text-align: left;"><div id="statusMessage">&nbsp;</div></td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <div class="dijitDialogPaneActionBar" style="text-align: left; padding-left: 11%;">
                                    <button data-dojo-type="dijit/form/Button" type="button">
                                        <script type="dojo/connect" data-dojo-event="onClick">
                                            Login.send(username.value,password.value);
                                        </script>
                                        Let me in
                                    </button>
                                </div>
                            </td>
                        </tr>

                    </table>


                </div>

                <div data-dojo-type="dijit/layout/ContentPane" data-dojo-props="region:'center'" style="height: 300px; text-align: center;">

                    Signup!, don't have an account yet? Etc... 

                </div>

            </div>

        </div>

    </body>
</html>
