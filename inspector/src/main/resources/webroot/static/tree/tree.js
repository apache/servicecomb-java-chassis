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
function Tree(id) {
  var tree = this;
  this.eleTree = document.getElementById(id);
  this.eleTree.onclick = function (event) {
    tree.onTreeClicked(event);
  };
  this.activeTreeNode = null;
  this.configId = id + "-config";

  var defaultConfig = {
    activeNodeId: null,
    expanded: {}
  };
  this.config = Object.assign(defaultConfig,
      JSON.parse(localStorage.getItem(this.configId)));
}

Tree.prototype.init = function () {
  // restore expanded
  for (var id in this.config.expanded) {
    var ele = document.getElementById(id);
    if (ele != null) {
      ele.setAttribute("expanded", this.config.expanded[id]);
    }
  }

  // click active node
  this.clickActiveTreeNode();
}

Tree.prototype.clickActiveTreeNode = function () {
  if (this.config.activeNodeId != null) {
    var treeNode = document.getElementById(this.config.activeNodeId);
    if (treeNode != null) {
      treeNode.querySelector(":scope > tree-face > tree-label").click();
      return;
    }
  }

  var treeLabel = this.eleTree.querySelector("tree-node:not([no-action]) > tree-face > tree-label");
  if (treeLabel != null) {
    treeLabel.click();
  }
}

Tree.prototype.saveConfig = function () {
  localStorage.setItem(this.configId, JSON.stringify(this.config));
}

Tree.prototype.onTreeClicked = function (event) {
  if (event.target.localName == "tree-label") {
    this.onTreeLabelClicked(event);
    return;
  }

  if (event.target.localName == "tree-node-open-close") {
    this.onTreeNodeOpenCloseClicked(event);
    return;
  }
}

Tree.prototype.onTreeNodeOpenCloseClicked = function (event) {
  var treeNode = event.target.parentElement.parentElement;
  var expanded = treeNode.getAttribute("expanded") == "true";
  expanded = !expanded;
  treeNode.setAttribute("expanded", expanded);

  var id = treeNode.getAttribute("id");
  if (id != null) {
    this.config.expanded[id] = expanded;
    this.saveConfig();
  }
}

Tree.prototype.onTreeLabelClicked = function (event) {
  var treeNode = event.target.parentElement.parentElement;
  if (treeNode.hasAttribute("no-action")) {
    return;
  }

  // switch active status
  if (this.activeTreeNode != null) {
    this.activeTreeNode.classList.remove("active");
  }
  this.activeTreeNode = treeNode;
  this.activeTreeNode.classList.add("active");

  // save status
  var id = this.activeTreeNode.getAttribute("id");
  if (id != null) {
    this.config.activeNodeId = id;
    this.saveConfig();
  }
}
