/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at

* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import static com.gradle.CiUtils.isGitHubActions
import static com.gradle.Utils.envVariable

// Add JVM Major version as tag
Runtime.Version version = Runtime.version()
buildScan.tag("Java ${version.feature()}")

// Add more details for CI builds
if (isGitHubActions()) {

  // Add Pull Request info to custom data
  envVariable("GITHUB_BASE_REF").ifPresent(value -> {
    buildScan.value("PR", "true")
    buildScan.value("PR Target Branch", value)
  })

  // 'Git branch' shows up as 'HEAD' sometimes on GH Actions
  envVariable("GITHUB_HEAD_REF").ifPresent(value ->
    buildScan.value("Git branch", value))

  // Add workflow name as tag
  envVariable("GITHUB_WORKFLOW").ifPresent(value ->
    buildScan.tag(value))

  // Add Build Scan info to step outputs
  envVariable("GITHUB_OUTPUT").ifPresent(value -> {
    buildScan.buildScanPublished({
      var id = it.buildScanId
      var uri = it.buildScanUri

      new File(value)
        << '\n'
        << "buildscan_id=${id}"
        << '\n'
        << "buildscan_uri=${uri}"
    })
  })

  // Add Build Scan link to CI result
  envVariable("GITHUB_STEP_SUMMARY").ifPresent(value ->
    buildScan.buildScanPublished({
      new File(value)
        << '\n'
        << "View the [Build Scan](${it.buildScanUri}) to see more information about this build."
    })
  )
}
