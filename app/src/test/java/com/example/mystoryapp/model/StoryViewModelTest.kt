package com.example.mystoryapp.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.mystoryapp.DataDummy
import com.example.mystoryapp.MainDispatcherRule
import com.example.mystoryapp.StoryViewModel
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.database.StoryRepository
import com.example.mystoryapp.getOrAwaitValue
import com.example.mystoryapp.preferences.UserPreference
import com.example.mystoryapp.ui.StoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class StoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: StoryViewModel
    private lateinit var repository: StoryRepository
    private lateinit var pref: UserPreference
    private lateinit var factory: ViewModelProvider.Factory

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        repository = mock(StoryRepository::class.java)
        pref = mock(UserPreference::class.java)
        viewModel = StoryViewModel(repository, pref)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when Get Story Should Not Be Null and Return Data`() {
        val repository = mock(StoryRepository::class.java)

        val dummyData = DataDummy.generateDummyStoryResponse()
        val pagingData: PagingData<ListStoryItem> = PagingData.from(dummyData)

        `when`(repository.getStoryPaging()).thenReturn(flowOf(pagingData))

        val viewModel = StoryViewModel(repository, pref)

        val result = viewModel.storiesResponse.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = NoOpListUpdateCallback
        )

        runBlocking {
            differ.submitData(result)
        }

        Assert.assertNotNull(result)
        Assert.assertNotNull(differ.snapshot())
        Assert.assertTrue(differ.snapshot().size > 0)
        assert(dummyData.size == differ.snapshot().size)
        assert(dummyData[0] == differ.snapshot()[0])
    }



    @Test
    fun `when Get Story Empty Should Return No Data`() {
        val emptyPagingData: PagingData<ListStoryItem> = PagingData.empty()
        `when`(repository.getStoryPaging()).thenReturn(flowOf(emptyPagingData))

        viewModel.getStories()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = NoOpListUpdateCallback
        )
        val snapshot = differ.snapshot()

        viewModel.storiesResponse.observeForever { pagingData ->
            runBlocking {
                differ.submitData(pagingData)
            }
        }

        assert(snapshot.size == 0)
    }
}

object NoOpListUpdateCallback : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
