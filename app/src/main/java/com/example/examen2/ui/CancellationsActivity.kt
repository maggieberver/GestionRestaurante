package com.example.examen2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examen2.adapters.CancellationAdapter
import com.example.examen2.data.DatabaseHelper
import com.example.examen2.databinding.ActivityCancellationsBinding

class CancellationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCancellationsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: CancellationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCancellationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = CancellationAdapter(dbHelper.getAllCancellations())
        binding.rvCancellations.layoutManager = LinearLayoutManager(this)
        binding.rvCancellations.adapter = adapter
    }
}
