package com.amplify.gradle

import org.gradle.api.GradleException
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

                def creds = settings.servers.server.find { it.id.equals(repo.id.text()) }

                if(creds.username.text()) {
                    credentials {
                        username creds.username.text()
                        password creds.password.text()
                    }
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
        final userHomeDirName = System.getProperty("user.home")
        if(userHomeDirName == null || userHomeDirName.isEmpty()) {
            throw new GradleException("No user home directory specified")
        }

        final settingsFile = new File(userHomeDirName, '.m2/settings.xml')
        if(!settingsFile.exists()) {
            throw new GradleException("Nothing found in ~/.m2/settings.xml")
        }

        new XmlSlurper().parse(settingsFile)
    }
}