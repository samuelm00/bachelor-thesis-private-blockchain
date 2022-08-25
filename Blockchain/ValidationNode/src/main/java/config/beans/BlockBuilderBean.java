package config.beans;

import blockBuilder.BlockBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repository.BlockRepository;

@Configuration
@RequiredArgsConstructor
public class BlockBuilderBean {
  private final BlockRepository blockRepository;

  @Bean
  public BlockBuilder blockBuilder() {
    return new BlockBuilder(blockRepository);
  }
}
