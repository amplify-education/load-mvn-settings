package com.amplify.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class MavenSettingsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def settings = getMavenSettingsCredentials()
        def activeProfile = settings.activeProfiles.activeProfile[0].text()
        def currentProfile = settings.profiles.profile.find { it.id.equals activeProfile }

        currentProfile.repositories.repository.each { repo ->
            def mavenRepo = {
                name repo.name.text()
                url repo.url.text()

                credentials {
                    def creds = settings.servers.server.find { it.id.equals(repo.id.text) }
                    username creds.username.text()
                    password creds.password.text()
                }
            }

            project.repositories {
                maven mavenRepo
            }

            project.buildscript {
                repositories {
                    maven mavenRepo
                }
            }
        }
    }

    def getMavenSettingsCredentials = {
        new XmlSlurper().parse(new File(System.getProperty("user.home"), '.m2/settings.xml'))
    }
}