# Docsite Maven Plugin

A Maven plugin that generates modern, static documentation sites as an alternative to the default Maven site.

## Features

- Generates clean, modern HTML documentation from Markdown files
- Automatic dark/light theme support
- Syntax highlighting for code blocks (via Prism.js)
- Mermaid diagram support
- Integrates with standard Maven reports (dependencies, licenses, surefire, etc.)
- Customizable sections and navigation
- Multi-module project support
- Automatic LICENSE and CHANGELOG detection

## Requirements

- Java 21 or higher
- Maven 3.9.x or higher

## Installation

You have to explicitly bind the goal to the `post-site` phase:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.myjtools</groupId>
            <artifactId>docsite-maven-plugin</artifactId>
            <version>1.0.2</version>
            <executions>
                <execution>
                    <phase>post-site</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Usage

Run the following command to generate the documentation site:

```bash
mvn site post-site
```

The site will be generated in `target/site` by default.

## Configuration

### Basic Configuration

```xml
<plugin>
    <groupId>org.myjtools</groupId>
    <artifactId>docsite-maven-plugin</artifactId>
    <version>1.0.2</version>
    <executions>
        <execution>
            <phase>post-site</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <outputFolder>${project.build.directory}/site</outputFolder>
        <logo>src/site/logo.png</logo>
        <skip>false</skip>
    </configuration>
</plugin>
```

### Custom Sections

You can add custom sections to your documentation site:

```xml
<configuration>
    <sections>
        <section>
            <title>Getting Started</title>
            <file>docs/getting-started.md</file>
            <icon>rocket</icon>
        </section>
        <section>
            <title>API Reference</title>
            <file>docs/api.md</file>
            <icon>book</icon>
        </section>
        <section>
            <title>Examples</title>
            <file>docs/examples.md</file>
            <icon>code</icon>
        </section>
    </sections>
</configuration>
```

Icons are from [Font Awesome 5](https://fontawesome.com/v5/search?m=free&s=solid). Use the icon name without the `fa-` prefix.

## Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `skip` | boolean | `false` | Skip the execution of the plugin |
| `outputFolder` | File | `${basedir}/target/site` | Output folder for the generated site |
| `logo` | File | `logo.png` | Logo file for the site header |
| `sections` | Section[] | - | Additional sections to include in the navigation |

### Section Configuration

| Parameter | Type | Description |
|-----------|------|-------------|
| `title` | String | Title displayed in the navigation menu |
| `file` | File | Path to the Markdown file |
| `icon` | String | Font Awesome icon name (without `fa-` prefix) |

## Automatic File Detection

The plugin automatically detects and includes the following files if they exist in your project root:

| File | Generated Page |
|------|----------------|
| `README.md` | `index.html` (required) |
| `LICENSE` | `license.html` |
| `CHANGELOG.md` | `changelog.html` |

## Maven Reports Integration

The plugin automatically integrates with Maven reports generated during the `site` phase. Reports are listed under the "Reports" section in the navigation menu.

To enable Maven reports, configure them in the `<reporting>` section of your `pom.xml`:

```xml
<reporting>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-project-info-reports-plugin</artifactId>
            <version>3.5.0</version>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-report-plugin</artifactId>
            <version>3.2.5</version>
        </plugin>
    </plugins>
</reporting>
```

## Multi-Module Projects

For multi-module projects, the plugin automatically detects modules and creates navigation links to each module's documentation.
Bear in mind that the site generation must be done before the staging/deployment of the site, 
so ensure to run `mvn site post-site` at the parent project level, and then, run `mvn site:stage` or `mvn site:deploy` as needed.

## Example

A complete example configuration:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.myjtools</groupId>
            <artifactId>docsite-maven-plugin</artifactId>
            <version>1.0.2</version>
            <executions>
                <execution>
                    <phase>post-site</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <logo>src/site/logo.png</logo>
                <sections>
                    <section>
                        <title>Installation</title>
                        <file>docs/installation.md</file>
                        <icon>download</icon>
                    </section>
                    <section>
                        <title>Configuration</title>
                        <file>docs/configuration.md</file>
                        <icon>cog</icon>
                    </section>
                </sections>
            </configuration>
        </plugin>
    </plugins>
</build>

<reporting>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-project-info-reports-plugin</artifactId>
            <version>3.5.0</version>
        </plugin>
    </plugins>
</reporting>
```

## License

This project is licensed under the [MIT License](http://repository.jboss.org/licenses/mit.txt).

## Author

Luis Inesta Gelabert - luiinge@gmail.com
