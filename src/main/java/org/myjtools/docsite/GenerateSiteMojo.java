package org.myjtools.docsite;

import j2html.TagCreator;
import j2html.tags.specialized.*;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import static j2html.TagCreator.*;

@Mojo(defaultPhase = LifecyclePhase.POST_SITE, name = "generate")
public class GenerateSiteMojo extends AbstractMojo {



    /**
     * Skips the execution for this project
     * @since 1.0
     */
    @Parameter(name = "skip", defaultValue = "false", property = "docsite.skip")
    boolean skip;


    /**
     * Output folder for the generated site
     * @since 1.0
     */
    @Parameter(name = "outputFolder", defaultValue = "${basedir}/target/site", property = "docsite.outputFolder")
    File outputFolder;

    /**
     * Logo file for the generated site
     * @since 1.0
     */
    @Parameter( name = "logo", property = "docsite.logo", defaultValue = "logo.png")
    File logo;


    /**
     * Additional sections to include in the generated site
     * @since 1.0
     */
    @Parameter( name = "sections" )
    Section[] sections;



    @Parameter( defaultValue = "${basedir}", readonly = true )
    File baseDir;

    @Parameter( defaultValue = "${project}", readonly = true )
    MavenProject project;

    @Parameter( defaultValue = "${session}", readonly = true )
    MavenSession session;

    private Path licenseFile;
    private Path changelogFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            if (skip) {
                return;
            }
            if (!Files.exists(baseDir.toPath().resolve("README.md"))) {
                getLog().warn("No README.md found in project base directory, skipping docsite generation");
                return;
            }

            Files.createDirectories(outputFolder.toPath());
            copyFileIfExists(logo.toPath(), "logo.png");
            licenseFile = baseDir.toPath().resolve("LICENSE");
            changelogFile = baseDir.toPath().resolve("CHANGELOG.md");

            getLog().info("Generating site in " + outputFolder.getAbsolutePath());
            generateSite();

        } catch (IOException e) {
            throw new MojoExecutionException("Error executing docsite generation", e);
        }
    }


    private void generateSite() throws IOException, MojoFailureException {

        NavTag siteTree = treeSite();

        boolean withLogo = (logo != null && logo.exists());
        PageTemplate template = new PageTemplate(project,siteTree,withLogo);
        getLog().info("Generating index.html from README.md");
        template.generate(baseDir.toPath().resolve("README.md"), outputFolder.toPath().resolve("index.html"));
        if (sections != null ) {
            for (Section section : sections) {
                if (!section.file.exists()) {
                    continue;
                }
                getLog().info("Generating "+section.title+ ".html from "+ section.file.getAbsolutePath());
                template.generate(section.file.toPath(), outputFolder.toPath().resolve(section.title + ".html"));
            }
        }

        if (Files.exists(licenseFile)) {
            getLog().info("Generating license.html from LICENSE");
            template.generate(licenseFile, outputFolder.toPath().resolve("license.html"));
        }
        if (Files.exists(changelogFile)) {
            getLog().info("Generating changelog.html from CHANGELOG.md");
            template.generate(changelogFile, outputFolder.toPath().resolve("changelog.html"));
        }
    }


    private NavTag treeSite() throws IOException {
        List<MavenReport> reports = extractMavenReports();

        UlTag reportsUl = ul();
        for (MavenReport report : reports) {
            reportsUl = reportsUl.with(li().with(a(report.title()).withHref(report.fileName()).withTarget("_blank")));
        }
        UlTag ul = TagCreator.ul();
        ul = ul.with(treeSiteEntry("index.html", "home", "Introduction"));


        if (project.getModules() != null && !project.getModules().isEmpty()) {
            UlTag modulesUl = ul();
            for (String moduleName : project.getModules()) {
                modulesUl = modulesUl.with(li().with(a(moduleName).withHref(moduleName + "/index.html").withTarget("_blank")));
            }
            ul = ul.with(li().with(a().with(i().withClass("fas fa-boxes")).withText("Modules")).with(modulesUl));
        } else if (sections != null ) {
            for (Section section : sections) {
                ul = ul.with(treeSiteEntry(section.title + ".html", section.icon, section.title));
            }
        }

        if (Files.exists(licenseFile)) {
            ul = ul.with(treeSiteEntry("license.html", "balance-scale", "License"));
        }
        if (Files.exists(changelogFile)) {
            ul = ul.with(treeSiteEntry("changelog.html", "history", "Changelog"));
        }
        if (!reports.isEmpty()) {
            ul = ul.with(li().with(a().with(i().withClass("fas fa-chart-bar")).withText("Reports")).with(reportsUl));
        }

        return TagCreator.nav().with(ul).withClass("site-tree-header");
    }


    private LiTag treeSiteEntry(String href, String icon, String title) {
        return li().with(a().withHref(href).with(i().withClass("fas fa-"+icon)).withText(title));
    }


    public record MavenReport(String fileName, String title) {}


    private List<MavenReport> extractMavenReports() throws IOException {
        Path siteDir = outputFolder.toPath();

        if (!Files.exists(siteDir.resolve("project-reports.html"))) {
            getLog().warn("Project reports not generated");
            return Collections.emptyList();
        }
        String content  = Files.readString(siteDir.resolve("project-reports.html"));

        List<MavenReport> reports = new ArrayList<>();
        Pattern reportPattern = Pattern.compile("<td><a href=\"(.*)\">(.*)</a></td>");
        Matcher matcher = reportPattern.matcher(content);
        while (matcher.find()) {
            getLog().info("Found report: " + matcher.group(2) + " -> " + matcher.group(1));
            String fileName = matcher.group(1).trim();
            String title = matcher.group(2).trim();
            reports.add(new MavenReport(fileName, title));
        }
        return reports;
    }


    private void copyFileIfExists(Path fileSource, String destFileName) throws IOException {
        if (!Files.exists(fileSource)) {
            return;
        }
        Path logoDest = outputFolder.toPath().resolve(destFileName);
        Files.copy(fileSource, logoDest, StandardCopyOption.REPLACE_EXISTING);
    }

}
