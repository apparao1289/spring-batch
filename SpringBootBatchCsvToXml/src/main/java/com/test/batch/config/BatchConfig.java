package com.test.batch.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import com.test.batch.model.Person;
import com.test.batch.processor.PersonItenProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	/*
	@Bean
	public JdbcCursorItemReader<Person> reader() {
		JdbcCursorItemReader<Person> cursorItemReader = new JdbcCursorItemReader<>();
		cursorItemReader.setDataSource(dataSource);
		cursorItemReader.setSql("SELECT person_id,first_name,last_name,email,age FROM person");
		cursorItemReader.setRowMapper(new PersonRowMapper());
		return cursorItemReader;
	}*/
	
	@Bean
    public FlatFileItemReader<Person> reader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
        reader.setResource(new ClassPathResource("persons.csv"));
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "firstName", "lastName","email","age" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});
        return reader;
    }

	@Bean
	public PersonItenProcessor processor() {
		return new PersonItenProcessor();
	}

	@Bean
	public StaxEventItemWriter<Person> writer() {
		StaxEventItemWriter<Person> writer = new StaxEventItemWriter<Person>();
		writer.setResource(new ClassPathResource("persons.xml"));

		Map<String, String> aliasesMap = new HashMap<String, String>();
		aliasesMap.put("person", "com.test.batch.model.Person");
		XStreamMarshaller marshaller = new XStreamMarshaller();
		marshaller.setAliases(aliasesMap);
		writer.setMarshaller(marshaller);
		writer.setRootTagName("persons");
		writer.setOverwriteOutput(true);
		return writer;
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<Person, Person>chunk(100)
				.reader(reader())
				.processor(processor())
				.writer(writer()).build();
	}

	@Bean
	public Job exportPerosnJob() {
		return jobBuilderFactory.get("exportPeronJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1()).end().build();
	}
}
