package tech.onlycoders.backend;

import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import tech.onlycoders.backend.exception.ApiException;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceTest {

  //	@Mock
  //	private PersonRepository personRepository;

  @Test
  public void shouldDoLogin() throws ApiException {
    System.out.println("TEST");
    //		EasyRandomParameters parameters = new EasyRandomParameters().excludeField(FieldPredicates.named("teammates"));
    //		var ezRandom = new EasyRandom(parameters);
    //		var person = ezRandom.nextObject(Person.class);
    //		Mockito.when(this.personRepository.findByName("Greg")).thenReturn(Optional.of(person));
  }
}
