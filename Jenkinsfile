@Library('sym-pipeline') _

node("build-directory-jenkins-agent") {

    String project = 'directory-scimple'
    String projectOrg = 'SymphonyOSF'
    withEnv([
            "GIT_REPO=${project}",
            "GIT_ORG=${projectOrg}",
            "GIT_BRANCH=${env.BRANCH_NAME}",
    ]) {

        stage('Build and test jar') {
            gitCheckout()
            mvn('clean package')
        }

        stage("Publish the packages") {
            deployOnArtfifactory('scim-server/target/scim-server-1.0.0-SNAPSHOT.jar', 'cip-local-dev/directory-scimple/')
        }
    }
}

def mvn(String cmd) {
    withCredentials([file(credentialsId: 'maven-settings', variable: 'settings_xml')]) {
        sh 'mkdir -p /home/jenkins/.m2'
        sh 'base64 -d \$settings_xml > /home/jenkins/.m2/settings.xml'
        sh "mvn -B ${cmd}"
    }
}

def deployOnArtfifactory(filename, target_folder) {
    echo "Deploying the Directory-scimple to Artifactory"
    def artifactoryServer = Artifactory.server 'Symphony-Production-Artifactory'
    def buildInfo = Artifactory.newBuildInfo()
    buildInfo.name = "${filename}"
    def uploadSpec = """{
        "files": [{
            "pattern": "${filename}",
            "target": "${target_folder}"
            }
            ]
    }"""
    artifactoryServer.upload(uploadSpec, true)
    artifactoryServer.publishBuildInfo(buildInfo)
}
