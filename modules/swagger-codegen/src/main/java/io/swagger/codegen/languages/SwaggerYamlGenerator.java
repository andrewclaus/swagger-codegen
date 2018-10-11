package io.swagger.codegen.languages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.swagger.codegen.*;
import io.swagger.codegen.examples.ExampleGenerator;
import io.swagger.jackson.mixin.ResponseSchemaMixin;
import io.swagger.models.Model;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.util.DeserializationModule;
import io.swagger.util.Yaml;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SwaggerYamlGenerator extends DefaultCodegen implements CodegenConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerYamlGenerator.class);

    public static final String OUTPUT_NAME = "outputFile";

    public static final String SWAGGER_FILENAME_DEFAULT_YAML = "swagger.yaml";

    protected String outputFile = SWAGGER_FILENAME_DEFAULT_YAML;


    public SwaggerYamlGenerator() {
        super();
        embeddedTemplateDir = templateDir = "swagger";
        outputFolder = "generated-code/swagger";

        cliOptions.add(new CliOption(OUTPUT_NAME,
                "output filename")
                .defaultValue(SWAGGER_FILENAME_DEFAULT_YAML));

        supportingFiles.add(new SupportingFile("README.md", "", "README.md"));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.DOCUMENTATION;
    }

    @Override
    public String getName() {
        return "swagger-yaml";
    }

    @Override
    public String getHelp() {
        return "Creates a static swagger.yaml file.";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(OUTPUT_NAME) && !StringUtils.isBlank((String) additionalProperties.get(OUTPUT_NAME))) {
            setOutputFile((String) additionalProperties.get(OUTPUT_NAME));
        }
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void processSwagger(Swagger swagger) {
        try {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
                    .configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
                    .configure(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS, true));
            configureMapper(mapper);
            String swaggerString = mapper.writeValueAsString(swagger);
            String outputFile = outputFolder + File.separator + this.outputFile;
            FileUtils.writeStringToFile(new File(outputFile), swaggerString);
            LOGGER.debug("wrote file to " + outputFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String escapeQuotationMark(String input) {
        // just return the original string
        return input;
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // just return the original string
        return input;
    }

    @Override
    protected List<Map<String, String>> getExamples(Map<String, Model> definitions, Map<String, Object> examples, List<String> produces, Object object) {
        if (examples == null || examples.isEmpty()) {
            return null;
        }
        return super.getExamples(definitions, examples, produces, object);
    }

    private void configureMapper(ObjectMapper mapper) {
        Module deserializerModule = new DeserializationModule(true, true);
        mapper.registerModule(deserializerModule);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.addMixIn(Response.class, ResponseSchemaMixin.class);
    }
}
