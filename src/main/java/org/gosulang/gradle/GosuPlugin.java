package org.gosulang.gradle;

import org.gosulang.gradle.tasks.GosuRuntime;
import org.gosulang.gradle.tasks.GosuSourceSet;
import org.gosulang.gradle.tasks.gosudoc.GosuDoc;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

public class GosuPlugin implements Plugin<Project> {

  public void apply(Project project) {
    project.getPluginManager().apply(GosuBasePlugin.class);
    project.getPluginManager().apply(JavaPlugin.class);

    refreshTestRuntimeClasspath(project);
    configureGosuDoc(project);
  }

  /**
   * Ensures that the runtime dependency on gosu-core is included the testRuntime's classpath
   * @param project
   */
  private void refreshTestRuntimeClasspath( final Project project ) {
    final JavaPluginConvention pluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
    GosuRuntime gosuRuntime = project.getExtensions().getByType(GosuRuntime.class);

    SourceSet main = pluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    SourceSet test = pluginConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

    test.setRuntimeClasspath(project.files(
        test.getOutput(),
        main.getOutput(),
        project.getConfigurations().getByName(JavaPlugin.TEST_RUNTIME_CONFIGURATION_NAME),
        gosuRuntime.inferGosuClasspath(project.getConfigurations().getByName(JavaPlugin.TEST_COMPILE_CONFIGURATION_NAME))));
  }

  private void configureGosuDoc( final Project project ) {
    GosuDoc gosuDoc = project.getTasks().create("gosudoc", GosuDoc.class);
    gosuDoc.setDescription("Generates Gosudoc API documentation for the main source code.");
    gosuDoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);

    JavaPluginConvention convention = project.getConvention().getPlugin(JavaPluginConvention.class);
    SourceSet sourceSet = convention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    gosuDoc.setClasspath(sourceSet.getOutput().plus(sourceSet.getCompileClasspath()));

    GosuSourceSet gosuSourceSet = new DslObject(sourceSet).getConvention().getPlugin(GosuSourceSet.class);
    gosuDoc.setSource(gosuSourceSet.getGosu());
  }

}
