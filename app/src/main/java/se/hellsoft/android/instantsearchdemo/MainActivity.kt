package se.hellsoft.android.instantsearchdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import se.hellsoft.android.instantsearchdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val searchAdapter = SearchAdapter()

    private val viewModel:SearchViewModel by viewModels { SearchViewModel.Factory(assets, Dispatchers.IO) }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.searchResult.adapter = searchAdapter
        viewModel.searchResult.observe(this) {
            when(it) {
                is ValidResult -> {
                    binding.otherResultText.visibility = View.GONE
                    binding.searchResult.visibility = View.VISIBLE
                    searchAdapter.submitList(it.result)
                }
                is ErrorResult -> {
                    searchAdapter.submitList(emptyList())
                    binding.otherResultText.visibility = View.VISIBLE
                    binding.searchResult.visibility = View.GONE
                    binding.otherResultText.setText(R.string.search_error)
                }
                is EmptyResult -> {
                    searchAdapter.submitList(emptyList())
                    binding.otherResultText.visibility = View.VISIBLE
                    binding.searchResult.visibility = View.GONE
                    binding.otherResultText.setText(R.string.empty_result)
                }
                is EmptyQuery -> {
                    searchAdapter.submitList(emptyList())
                    binding.otherResultText.visibility = View.VISIBLE
                    binding.searchResult.visibility = View.GONE
                    binding.otherResultText.setText(R.string.not_enough_characters)
                }
            }
        }

        binding.searchText.doAfterTextChanged { viewModel.search(it.toString()) }
    }

    class SearchAdapter : ListAdapter<String, SearchViewHolder>(DIFF_CALLBACK) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return SearchViewHolder(layoutInflater.inflate(R.layout.search_item, parent, false))
        }

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        companion object {
            private val DIFF_CALLBACK = object  : DiffUtil.ItemCallback<String>() {
                override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem

                override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            }
        }
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(text: String) {
            val textView = itemView.findViewById<TextView>(R.id.resultText)
            textView.text = text
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

