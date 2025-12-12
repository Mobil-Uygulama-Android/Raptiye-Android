package tr.edu.bilimankara20307006.taskflow.data.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.ProjectStatus


/**
 * ProjectManager için Unit Test sınıfı
 * iOS ekibinin ProjectManagerTests.swift dosyasına karşılık gelir
 * 
 * Test edilen özellikler:
 * ✅ ProjectManager başlangıç değerleri
 * ✅ Project modeli oluşturma ve özellikleri
 * ✅ ProjectStatus enum testleri (ACTIVE/COMPLETED/ARCHIVED)
 * ✅ Task ilerleme hesaplaması (progressPercentage)
 * ✅ Loading ve error state yönetimi
 * ✅ Projects dizisi operasyonları
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectManagerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockDb: FirebaseFirestore
    private lateinit var projectManager: ProjectManager

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock Firebase services to prevent actual Firebase initialization
        mockAuth = mockk(relaxed = true)
        mockDb = mockk(relaxed = true)
        
        // Setup basic mock behavior
        every { mockAuth.currentUser } returns null
        
        // Create ProjectManager with mocked dependencies
        projectManager = ProjectManager(auth = mockAuth, db = mockDb)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ✅ Test 1: ProjectManager başlangıç değerleri
    @Test
    fun `initial state should have empty projects list`() {
        val projects = projectManager.projects.value
        val isLoading = projectManager.isLoading.value
        val errorMessage = projectManager.errorMessage.value
        
        assertTrue("Initial projects list should be empty", projects.isEmpty())
        assertFalse("Initial loading should be false", isLoading)
        assertNull("Initial error message should be null", errorMessage)
    }

    // ✅ Test 2: Project modeli oluşturma ve temel özellikler
    @Test
    fun `project model should be created with correct properties`() {
        val project = Project(
            id = "test-id",
            title = "Test Project",
            description = "Test Description",
            iconName = "folder",
            iconColor = "blue",
            status = ProjectStatus.ACTIVE,
            tasksCount = 10,
            completedTasksCount = 3
        )
        
        assertEquals("Project ID should match", "test-id", project.id)
        assertEquals("Project title should match", "Test Project", project.title)
        assertEquals("Project description should match", "Test Description", project.description)
        assertEquals("Project icon should match", "folder", project.iconName)
        assertEquals("Project color should match", "blue", project.iconColor)
        assertEquals("Project status should be ACTIVE", ProjectStatus.ACTIVE, project.status)
        assertFalse("Project should not be completed initially", project.isCompleted)
    }

    // ✅ Test 3: ProjectStatus enum testleri
    @Test
    fun `project status enum should have all cases`() {
        val statuses = ProjectStatus.values()
        
        assertEquals("Should have 3 status types", 3, statuses.size)
        assertTrue("Should contain ACTIVE", statuses.contains(ProjectStatus.ACTIVE))
        assertTrue("Should contain COMPLETED", statuses.contains(ProjectStatus.COMPLETED))
        assertTrue("Should contain ARCHIVED", statuses.contains(ProjectStatus.ARCHIVED))
    }

    // ✅ Test 4: Task ilerleme yüzdesi hesaplaması
    @Test
    fun `progress percentage should be calculated correctly`() {
        // Tamamlanmış görev yok
        val project1 = Project(
            title = "Project 1",
            description = "Test",
            tasksCount = 10,
            completedTasksCount = 0
        )
        assertEquals("0% progress when no tasks completed", 0.0, project1.progressPercentage, 0.001)
        
        // Yarısı tamamlanmış
        val project2 = Project(
            title = "Project 2",
            description = "Test",
            tasksCount = 10,
            completedTasksCount = 5
        )
        assertEquals("50% progress when half completed", 0.5, project2.progressPercentage, 0.001)
        
        // Tamamı tamamlanmış
        val project3 = Project(
            title = "Project 3",
            description = "Test",
            tasksCount = 10,
            completedTasksCount = 10
        )
        assertEquals("100% progress when all completed", 1.0, project3.progressPercentage, 0.001)
        
        // Hiç görev yok
        val project4 = Project(
            title = "Project 4",
            description = "Test",
            tasksCount = 0,
            completedTasksCount = 0
        )
        assertEquals("0% progress when no tasks", 0.0, project4.progressPercentage, 0.001)
    }

    // ✅ Test 5: Due date formatting
    @Test
    fun `due date should be formatted correctly`() {
        val project = Project(
            title = "Test Project",
            description = "Test",
            dueDate = "2024-12-31"
        )
        
        assertNotNull("Due date should not be null", project.dueDate)
        assertEquals("Formatted due date should match", "2024-12-31", project.formattedDueDate)
    }

    // ✅ Test 6: Project with null due date
    @Test
    fun `project with null due date should return empty string`() {
        val project = Project(
            title = "Test Project",
            description = "Test",
            dueDate = null
        )
        
        assertNull("Due date should be null", project.dueDate)
        assertEquals("Formatted due date should be empty", "", project.formattedDueDate)
    }

    // ✅ Test 7: Project status değişimi
    @Test
    fun `project status can be updated`() {
        val project = Project(
            title = "Test Project",
            description = "Test",
            status = ProjectStatus.ACTIVE
        )
        
        assertEquals("Initial status should be ACTIVE", ProjectStatus.ACTIVE, project.status)
        
        val updatedProject = project.copy(status = ProjectStatus.ARCHIVED)
        assertEquals("Updated status should be ARCHIVED", 
            ProjectStatus.ARCHIVED, updatedProject.status)
        
        val completedProject = updatedProject.copy(
            status = ProjectStatus.COMPLETED,
            isCompleted = true
        )
        assertEquals("Final status should be COMPLETED", 
            ProjectStatus.COMPLETED, completedProject.status)
        assertTrue("Project should be marked as completed", completedProject.isCompleted)
    }

    // ✅ Test 8: Task count updates
    @Test
    fun `task counts should update progress correctly`() {
        val project = Project(
            title = "Test Project",
            description = "Test",
            tasksCount = 5,
            completedTasksCount = 2
        )
        
        assertEquals("Should have 5 total tasks", 5, project.tasksCount)
        assertEquals("Should have 2 completed tasks", 2, project.completedTasksCount)
        assertEquals("Progress should be 40%", 0.4, project.progressPercentage, 0.001)
        
        // Bir görev daha tamamlandı
        val updatedProject = project.copy(completedTasksCount = 3)
        assertEquals("Should have 3 completed tasks", 3, updatedProject.completedTasksCount)
        assertEquals("Progress should be 60%", 0.6, updatedProject.progressPercentage, 0.001)
    }

    // ✅ Test 9: Loading state kontrolü
    @Test
    fun `loading state should be false initially`() {
        assertFalse("Loading should be false initially", projectManager.isLoading.value)
    }

    // ✅ Test 10: Error message kontrolü
    @Test
    fun `error message should be null initially`() {
        assertNull("Error message should be null initially", projectManager.errorMessage.value)
    }
}
