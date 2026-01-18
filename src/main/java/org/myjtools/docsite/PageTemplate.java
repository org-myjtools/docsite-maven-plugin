package org.myjtools.docsite;

import j2html.TagCreator;
import j2html.tags.DomContent;
import j2html.tags.specialized.*;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.myjtools.doc2html.Feature;
import org.myjtools.doc2html.MarkdownHtmlGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static j2html.TagCreator.*;
import static j2html.TagCreator.a;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.i;
import static j2html.TagCreator.nav;

public class PageTemplate {

    private final MavenProject project;
    private final NavTag siteTree;
    private final boolean logo;
    MarkdownHtmlGenerator mdGenerator;

    PageTemplate(MavenProject project, NavTag siteTree, boolean logo) {
        this.project = project;
        this.siteTree = siteTree;
        this.logo = logo;
        this.mdGenerator = new MarkdownHtmlGenerator();
        mdGenerator.setFeature(Feature.GITHUB_EMOJIS, true);
        mdGenerator.setFeature(Feature.MERMAID_DIAGRAMS, true);
        mdGenerator.setFeature(Feature.FONT_AWESOME_ICONS, true);
        mdGenerator.setFeature(Feature.SYNTAX_HIGHLIGHTING, true);
    }

    public void generate(Path markdownFile, Path outputFile) throws IOException, MojoFailureException {

        var indexDoc = mdGenerator.transform(Files.newBufferedReader(markdownFile));
        try (var writer = Files.newBufferedWriter(outputFile)) {
            String html =
                html().with(
                    indexDoc.head().with(style()),
                    body().with(
                        header(),
                        div().withClass("layout-container").with(
                            siteTree.withClass("site-tree"),
                            indexDoc.main(),
                            indexDoc.tableOfContents(2,3))
                        ),
                        script()
                ).render();

            Pattern imagePattern = Pattern.compile("<img src=\"(.*?)\" alt=\".*?\" ?/?>");
            Matcher imagesMatcher = imagePattern.matcher(html);
            if (logo) {
                imagesMatcher.find(); // skip the first image which is the logo
            }
            while (imagesMatcher.find()) {
                String imgSrc = imagesMatcher.group(1);
                Path imgSourcePath = markdownFile.getParent().resolve(imgSrc).normalize();
                Path imgDestPath = outputFile.getParent().resolve(imgSrc).normalize();
                Files.copy(imgSourcePath, imgDestPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            }

            writer.write(html);
        }
    }


    private StyleTag style() throws MojoFailureException {
        String css = readResourceAsString("style.css");
        return TagCreator.style(css);
    }


    private ScriptTag script() throws MojoFailureException {
        String js = readResourceAsString("script.js");
        return TagCreator.script(js);
    }


    private HeaderTag header() {
        return TagCreator.header().withClass("top-bar").with(
                div().withClass("top-bar-title").with(logo(), span(project.getName())),
                div().withClass("top-bar-social").with(
                        button().withClass("theme-toggle").withId("theme-toggle").attr("aria-label", "Toggle Dark/Light Mode").with(
                                i().withClass("fa fa-moon").withId("theme-icon")
                        ),
                        a().withTarget("_blank").withHref(project.getUrl()).withRel("noopener noreferrer").with(
                                i().withClass("fab fa-github")
                        )
                )
        );
    }


    private DomContent logo() {
        if (logo) {
            return img().withSrc("logo.png").withAlt("Logo").withClass("logo-icon");
        } else {
            return div();
        }
    }

    private String readResourceAsString(String resourceName) throws MojoFailureException {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new MojoFailureException(resourceName + " resource not found");
            }
            return new String(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new MojoFailureException("Cannot read " + resourceName, e);
        }
    }



}
