/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function initSchemas() {
  var eleFormat = document.getElementById("schema-format");
  eleFormat.onchange = function () {
    localStorage.setItem("schemaFormatIdx", eleFormat.selectedIndex);
    if (mainTree.config.activeNodeId != null && mainTree.config.activeNodeId.startsWith("schemas://")) {
      mainTree.clickActiveTreeNode();
    }
  };
  var formatIdx = localStorage.getItem("schemaFormatIdx");
  if (formatIdx != null) {
    eleFormat.selectedIndex = formatIdx;
  }

  ajaxJsonGet("schemas", initSchemasTree);
}

function initSchemasTree(schemas) {
  schemas = schemas.sort();
  var eleSchemas = document.getElementById("schemas");
  var childrenContent = "<tree-children>";
  for (var idx in schemas) {
    childrenContent += '<tree-node id="schemas://' + schemas[idx] + '">' +
        ' <tree-face>' +
        '  <tree-label onclick="clickShowSchema(event)">' + schemas[idx] + '</tree-label>' +
        '  <div class="download" onclick="clickDownloadSchema(event)"></div>' +
        ' </tree-face>' +
        '</tree-node>';
  }
  childrenContent += '</tree-children>';
  eleSchemas.insertAdjacentHTML("beforeend", childrenContent);

  mainTree.init();
}

function fetchAndshowSchemaAsSwagger(url) {
  ajaxGet(url, function (schema) {
    frame.editor.specActions.updateSpec(schema);
  });
}

function showSchemaAsSwagger(url) {
  if (frame.SwaggerEditorBundle != null) {
    // reuse swagger editor
    fetchAndshowSchemaAsSwagger(url);
    return;
  }

  ajaxGet(url, function (schema) {
    localStorage.setItem("swagger-editor-content", schema);
  });
  // this cdn return html as text/plain, so we must let iframe load it as html
  ajaxGet("https://cdn.jsdelivr.net/npm/swagger-editor-dist@3.6.24/index.html",
      function (html) {
        html = html.replace('<style>',
            '<base href="https://cdn.jsdelivr.net/npm/swagger-editor-dist@3.6.24/">\n'
            + '  <style>\n'
            + '  .swagger-ui .info .title {'
            + '    word-break: break-all;\n'
            + '  }\n');
        frame.location = getBlobURL(html, "text/html");
      });
}

function clickShowSchema(event) {
  var schemaId = event.target.innerText;
  localStorage.setItem("schemaId", schemaId);

  var eleFormat = document.getElementById("schema-format");
  var format = eleFormat.options[eleFormat.selectedIndex].value;
  var url = "schemas/" + schemaId + "?download=false&format=" + format;
  if (format == "SWAGGER") {
    showSchemaAsSwagger(url);
    return;
  }

  if (format == "HTML") {
    frame.location = url;
  }
}

function clickDownloadSchema(event) {
  var schemaId = event.target.previousElementSibling.innerText;
  var eleFormat = document.getElementById("schema-format");
  var format = eleFormat.options[eleFormat.selectedIndex].value;

  window.location = "schemas/" + schemaId + "?download=true&format=" + format;
}

function clickDownloadAllSchemas(event) {
  var eleFormat = document.getElementById("schema-format");
  var format = eleFormat.options[eleFormat.selectedIndex].value;

  window.location = "download/schemas?format=" + format;
}