/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.projectresult;

import org.gradle.api.artifacts.component.BuildIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.artifacts.ProjectComponentIdentifierInternal;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.DependencyGraphNode;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.DependencyGraphVisitor;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.RootGraphNode;
import org.gradle.api.internal.project.ProjectState;
import org.gradle.api.internal.project.ProjectStateRegistry;
import org.gradle.util.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to track which configurations in other projects a given resolution depends on. This data is
 * used to mark those configurations as observed so that they cannot be mutated later.
 *
 * TODO: This logic should be integrated directly into the DefaultLocalConfigurationMetadataBuilder so that
 * we instantly mark configurations as observed as their metadata is constructed. This is an improvement
 * over this visitor, where we only mark a configuration observed if its metadata is present in the final graph.
 * There are likely scenarios that this visitor does not cover, where a configuration's metadata is observed but
 * its component is not present in the final graph.
 */
public class ResolvedLocalComponentsResultGraphVisitor implements DependencyGraphVisitor {
    private final List<ResolvedProjectConfiguration> resolvedProjectConfigurations = new ArrayList<>();
    private final BuildIdentifier thisBuild;
    private final ProjectStateRegistry projectStateRegistry;
    private ComponentIdentifier rootId;

    public ResolvedLocalComponentsResultGraphVisitor(BuildIdentifier thisBuild, ProjectStateRegistry projectStateRegistry) {
        this.thisBuild = thisBuild;
        this.projectStateRegistry = projectStateRegistry;
    }

    @Override
    public void start(RootGraphNode root) {
        this.rootId = root.getOwner().getComponentId();
    }

    @Override
    public void visitNode(DependencyGraphNode node) {
        ComponentIdentifier componentId = node.getOwner().getComponentId();
        if (!rootId.equals(componentId) && componentId instanceof ProjectComponentIdentifierInternal) {
            ProjectComponentIdentifierInternal projectComponentId = (ProjectComponentIdentifierInternal) componentId;
            if (projectComponentId.getBuild().equals(thisBuild)) {
                resolvedProjectConfigurations.add(new ResolvedProjectConfiguration(projectComponentId.getIdentityPath(), node.getResolvedConfigurationId().getConfiguration()));
            }
        }
    }

    /**
     * Mark all visited project variant nodes as observed.
     */
    public void complete(ConfigurationInternal.InternalState requestedState) {
        for (ResolvedLocalComponentsResultGraphVisitor.ResolvedProjectConfiguration projectResult : resolvedProjectConfigurations) {
            ProjectState targetState = projectStateRegistry.stateFor(projectResult.projectIdentity);
            targetState.applyToMutableState(project -> {
                ConfigurationInternal targetConfig = (ConfigurationInternal) project.getConfigurations().findByName(projectResult.targetConfiguration);
                if (targetConfig != null) {
                    // Can be null when dependency metadata for target project has been loaded from cache
                    targetConfig.markAsObserved(requestedState);
                }
            });
        }
    }

    private static class ResolvedProjectConfiguration {
        private final Path projectIdentity;
        private final String targetConfiguration;

        public ResolvedProjectConfiguration(Path projectIdentity, String targetConfiguration) {
            this.projectIdentity = projectIdentity;
            this.targetConfiguration = targetConfiguration;
        }
    }
}
