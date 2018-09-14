#!/usr/bin/groovy
import static java.util.Locale.ROOT

folder('TDI') {
    displayName("[TDI] Folder containing TDI jobs")
    description("""""")
}

def addDockerTagProposal(image) {
    def paramName = image.replace('/', '_').replace('-', '').toUpperCase(ROOT) + '__DOCKER_IMAGE'

    activeChoiceParam(paramName) {
        description("The $image docker image tag")
        filterable()
        choiceType('SINGLE_SELECT')
        groovyScript {
            script("""
            $artifactoryClass

            return '[' + new Artifactory(token: \\"$token\\").listTags(\\"$image\\").collect { "\\"\$it\\"" }.join(',') + ']'
            """.stripMargin())
            fallbackScript('"No choice available')
        }
    }
}

/**
 * JOB DEFINITIONS
 */

pipelineJob('TDI/refresh-docker-images') {
    displayName("[TDI][generated] Refreshes images for data-catalog-stack")
    description("## Refreshes proposals for Data Catalog Stack\n\nWARNING: generated job, (last update: ${new Date().toString()}).")

    logRotator(30, -1, 1, -1)

    definition {
        cpsScm {
            scm {
                git {
                    branch('master')
                    remote {
                        github('talend/connectors-se', 'https')
                        credentials('github-credentials')
                    }
                }
                scriptPath('.jenkins/jobs/generated/RefreshDockerImages.groovy')
            }
        }
    }
}

/*
def createDataCatalogStack = readFileFromWorkspace('.jenkins/jobs/templates/createDataCatalogStack.groovy')
pipelineJob('TDI/data-catalog-stack') {
    displayName("[TDI][generated] Create Data Catalog Stack")
    description("## Build Data Catalog Stack\n\nWARNING: generated job.")

    parameters {
        addDockerTagProposal('talend/data-catalog/dataset')
        // duplicate all parameters (docker images) used as variables in createDataCatalogStack.groovy
    }

    logRotator(30, -1, 1, -1)

    definition {
        cps {
            script(createDataCatalogStack)
            sandbox()
        }
    }
}
*/