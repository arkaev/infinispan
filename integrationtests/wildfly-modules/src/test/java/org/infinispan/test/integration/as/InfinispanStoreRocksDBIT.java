package org.infinispan.test.integration.as;

import static java.io.File.separator;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.infinispan.Cache;
import org.infinispan.Version;
import org.infinispan.commons.util.Util;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.rocksdb.configuration.RocksDBStoreConfigurationBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.se.manifest.ManifestDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the Infinispan RocksDB CacheStore AS module integration
 *
 * @author Tristan Tarrant
 * @since 7.0
 */
@RunWith(Arquillian.class)
public class InfinispanStoreRocksDBIT {
   private static String baseDir = tmpDirectory(InfinispanStoreRocksDBIT.class);
   private static String dataDir = baseDir + File.separator + "data";
   private static String expiredDir = baseDir + File.separator + "expired";

   private EmbeddedCacheManager cm;

   @Deployment
   public static Archive<?> deployment() {
      return ShrinkWrap.create(WebArchive.class, "rocksdb.war").addClass(InfinispanStoreRocksDBIT.class).add(manifest(), "META-INF/MANIFEST.MF");
   }

   private static Asset manifest() {
      String manifest = Descriptors.create(ManifestDescriptor.class)
            .attribute("Dependencies", "org.infinispan:" + Version.getModuleSlot() + " services, org.infinispan.persistence.rocksdb:" + Version.getModuleSlot() + " services").exportAsString();
      return new StringAsset(manifest);
   }

   @Before
   @After
   public void removeDataFilesIfExists() {
      Util.recursiveFileRemove(baseDir);
      if (cm != null)
         cm.stop();
   }

   /**
    * To avoid pulling in TestingUtil and its plethora of dependencies
    */
   private static String tmpDirectory(Class<?> test) {
      String prefix = System.getProperty("infinispan.test.tmpdir", System.getProperty("java.io.tmpdir"));
      return prefix + separator + "infinispanTempFiles" + separator + test.getSimpleName();
   }

   @Test
   public void testCacheManager() {
      GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();

      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.persistence()
            .addStore(RocksDBStoreConfigurationBuilder.class)
            .location(dataDir)
            .expiredLocation(expiredDir);

      cm = new DefaultCacheManager(gcb.build(), builder.build());
      Cache<String, String> cache = cm.getCache();
      cache.put("a", "a");
      assertEquals("a", cache.get("a"));
   }
}
