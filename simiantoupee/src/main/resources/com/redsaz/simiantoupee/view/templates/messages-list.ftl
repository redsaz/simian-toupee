<#--
 Copyright 2016 Redsaz <redsaz@gmail.com>.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
<#escape x as x?html>
      <div class="row">
        <div class="col-sm-12 col-md-12 main">
          <h1 class="page-header">Messages</h1>

          <div class="resizable">
            <table class="table table-striped messages">
              <tbody>
                <#list messages as message>
                <tr>
                  <td class="message">
                    <div>
                      <a href="messages/${message.id}">${message.subject}</a> - ${message.body}</td>
                    </div>
                  <td>
                    <form action="${base}/messages/delete" method="POST">
                      <input type="hidden" name="id" value="${message.id}"/>
                      <button type="submit" class="btn btn-link glyphicon glyphicon-trash" style="padding: 1px 1px 1px 1px;"><span class="sr-only">Trash</span></button>
                    </form>
                  </td>
                </tr>
                </#list>
              </tbody>
            </table>
          </div>
        </div>
      </div>
</#escape>
