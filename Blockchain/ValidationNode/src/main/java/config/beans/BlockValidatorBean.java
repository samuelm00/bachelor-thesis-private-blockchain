package config.beans;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repository.BlockRepository;
import validation.BlockValidator;

@Configuration
@RequiredArgsConstructor
public class BlockValidatorBean {
  private final BlockRepository blockRepository;

  @Bean
  public BlockValidator blockValidator() {
    return new BlockValidator(blockRepository);
  }
}
