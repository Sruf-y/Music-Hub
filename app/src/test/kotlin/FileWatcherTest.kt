import ViewModels.FileWatcher
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.fail
import kotlin.test.junit.JUnitAsserter.assertNotNull


class FileWatcherTest {
   
   @Rule
   @JvmField
   val tempFolder = TemporaryFolder()
   
   class MockContext(tempFolder: TemporaryFolder) {
      val filesDir: File by lazy {
         tempFolder.newFolder("mock_files")
      }
      val cacheDir: File by lazy {
         tempFolder.newFolder("mock_cache")
      }
   }
      
      @Test
      fun testFileWatcherCreation() {
         // Create a temporary directory for testing
         val testDir: File = tempFolder.newFolder("test_watcher")
         
         // Test creating FileWatcher - should NOT crash
         val watcher = FileWatcher(testDir)
         assertNotNull("FileWatcher should be created", watcher)
      }
      
      @Test
      fun testNoStackOverflow() {
         val testDir = tempFolder.newFolder("test_stack")
         
         // Create multiple instances to test for recursion
         repeat(100) { iteration ->
            try {
               val watcher = FileWatcher(testDir)
               // If you have start/stop methods
               //watcher.startWatching()
               //watcher.stopWatching()
            } catch (e: StackOverflowError) {
               fail("StackOverflow at iteration $iteration")
            }
         }
      }
      
      @Test
      fun testLazyInitializationPattern() {
         val testDir = tempFolder.newFolder("test_lazy")
         
         // Simulate your app's lazy initialization
         var initializationCount = 0
         val lazyWatcher by lazy {
            initializationCount++
            FileWatcher(testDir).apply {
               //startWatching()
            }
         }
         
         // First access should initialize
         val watcher1 = lazyWatcher
         assertEquals(1, initializationCount)
         
         // Second access should use cached instance
         val watcher2 = lazyWatcher
         assertEquals(1, initializationCount)  // Should NOT increase
         assertSame(watcher1, watcher2)  // Should be same instance
      }
   
   }
