/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.ivyservice.projectmodule;

import org.gradle.api.artifacts.component.BuildIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.internal.DomainObjectContext;
import org.gradle.api.internal.artifacts.ProjectComponentIdentifierInternal;
import org.gradle.api.internal.artifacts.configurations.ProjectDependencyObservedListener;
import org.gradle.internal.build.BuildState;
import org.gradle.internal.component.local.model.LocalComponentGraphResolveState;
import org.gradle.internal.event.ListenerManager;
import org.gradle.util.Path;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Default implementation of {@link LocalComponentRegistry}. This is a simple project-scoped wrapper
 * around {@link BuildTreeLocalComponentProvider} that contextualizes it to the current project. The
 * primary purpose of this class is to track dependencies between projects as they are resolved.
 */
public class DefaultLocalComponentRegistry implements LocalComponentRegistry {
    private final Path currentProjectPath;
    private final BuildIdentifier thisBuild;
    private final ProjectDependencyObservedListener projectDependencyObservedListener;
    private final BuildTreeLocalComponentProvider componentProvider;

    @Inject
    public DefaultLocalComponentRegistry(
        DomainObjectContext domainObjectContext,
        BuildState currentBuild,
        ListenerManager listenerManager,
        BuildTreeLocalComponentProvider componentProvider
    ) {
        this.currentProjectPath = getProjectIdentityPath(domainObjectContext);
        this.thisBuild = currentBuild.getBuildIdentifier();
        this.projectDependencyObservedListener = listenerManager.getBroadcaster(ProjectDependencyObservedListener.class);
        this.componentProvider = componentProvider;
    }

    @Override
    public LocalComponentGraphResolveState getComponent(ProjectComponentIdentifier projectIdentifier) {
        Path targetProjectPath = ((ProjectComponentIdentifierInternal) projectIdentifier).getIdentityPath();
        if (!targetProjectPath.equals(currentProjectPath)) {
            projectDependencyObservedListener.projectObserved(currentProjectPath, targetProjectPath);
        }

        return componentProvider.getComponent(projectIdentifier, thisBuild);
    }

    @Nullable
    private static Path getProjectIdentityPath(DomainObjectContext domainObjectContext) {
        if (domainObjectContext.getProject() != null) {
            return domainObjectContext.getProject().getIdentityPath();
        }

        return null;
    }
}
