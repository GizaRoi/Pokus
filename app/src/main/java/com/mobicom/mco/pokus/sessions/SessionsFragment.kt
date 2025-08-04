package com.mobicom.mco.pokus.sessions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.data.repository.PokusRepository
import com.mobicom.mco.pokus.databinding.ActivitySessionsBinding
import com.mobicom.mco.pokus.services.TimerService
import com.mobicom.mco.pokus.todo.TodoItem
import com.mobicom.mco.pokus.ui.post.SaveSessionActivity
import java.util.concurrent.TimeUnit

class SessionsFragment : Fragment() {

    private var _binding: ActivitySessionsBinding? = null
    private val binding get() = _binding!!

    private var timerService: TimerService? = null
    private var isBound = false
    private lateinit var serviceIntent: Intent

    private lateinit var repository: PokusRepository
    private lateinit var sessionTaskAdapter: SessionTaskAdapter
    private lateinit var sessionTaskList: MutableList<TodoItem>

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true
            updateUI()
            setupObservers()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySessionsBinding.inflate(inflater, container, false)
        serviceIntent = Intent(requireContext(), TimerService::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = PokusRepository.getInstance(requireContext())
        sessionTaskList = repository.getSessionTasks()
        sessionTaskAdapter = SessionTaskAdapter(sessionTaskList, requireContext())
        binding.sessionTodoRecyclerView.adapter = sessionTaskAdapter
        binding.sessionTodoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        SessionDataHolder.tasksToImport?.let { tasks ->
            tasks.forEach { task ->
                if (sessionTaskList.none { it.id == task.id }) {
                    sessionTaskAdapter.addItem(task)
                    repository.addTaskToSession(task.id)
                }
            }
            SessionDataHolder.tasksToImport = null
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            requireActivity().unbindService(connection)
            isBound = false
        }
    }

    private fun updateUI() {
        timerService?.let {
            binding.timerText.text = formatTime(it.timeLeftInMillis)

            binding.startBtn.text = when {
                it.isTimerRunning -> "Pause"
                it.hasSessionStarted() -> "Continue"
                else -> "Start"
            }

            // **THE FIX**: Use hasSessionStarted() to control visibility
            setSessionControlsVisibility(it.hasSessionStarted())
            sessionTaskAdapter.setTimerRunning(it.isTimerRunning)
        }
    }

    private fun setSessionControlsVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.finishBtn.visibility = visibility
        binding.sessionActionsLayout.visibility = visibility
    }

    private fun setupClickListeners() {
        binding.startBtn.setOnClickListener {
            if (timerService?.isTimerRunning == true) {
                timerService?.pauseTimer()
            } else {
                requireActivity().startService(serviceIntent)
                timerService?.startTimer()
            }
            updateUI()
        }

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && isBound) {
                val duration = when (checkedId) {
                    R.id.shortBreakBtn -> TimerService.SHORT_BREAK_DURATION
                    R.id.longBreakBtn -> TimerService.LONG_BREAK_DURATION
                    else -> TimerService.POMODORO_DURATION
                }
                timerService?.resetTimer(duration)
                updateUI()
            }
        }

        binding.finishBtn.setOnClickListener {
            if (isBound) {
                val durationInMillis = timerService?.getSessionDuration() ?: 0L
                val tasksDoneCount = sessionTaskList.count { it.isChecked }

                // This line defines the variable 'totalTasksCount'
                val totalTasksCount = sessionTaskList.size
                val completedTasks = sessionTaskList.filter { it.isChecked }

                // This code uses the variable to pass data to the next screen
                val intent = Intent(requireContext(), SaveSessionActivity::class.java).apply {
                    putExtra("SESSION_DURATION", durationInMillis)
                    putExtra("TASKS_DONE_COUNT", tasksDoneCount)
                    putExtra("TOTAL_TASKS_COUNT", totalTasksCount)
                    putParcelableArrayListExtra("COMPLETED_TASKS_LIST", ArrayList(completedTasks))
                }
                startActivity(intent)

                // Stop and reset the service/UI
                timerService?.stopAndResetSession()
                requireActivity().stopService(serviceIntent)
                updateUI()
                sessionTaskList.clear()
                sessionTaskAdapter.notifyDataSetChanged()
            }
        }

        binding.btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        binding.btnImportTask.setOnClickListener {
            showImportTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Task")
            .setMessage("What new task do you want to add?")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val taskTitle = input.text.toString()
                if (taskTitle.isNotEmpty()) {
                    val newTask = TodoItem(title = taskTitle, isChecked = false)
                    val newId = repository.addTask(newTask)
                    val itemWithId = newTask.copy(id = newId)
                    sessionTaskAdapter.addItem(itemWithId)
                    repository.addTaskToSession(itemWithId.id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showImportTaskDialog() {
        val allTasks = repository.getAllTasks()
        val tasksToShow = allTasks.filter { task -> sessionTaskList.none { it.id == task.id } }.toTypedArray()
        val taskTitles = tasksToShow.map { it.title }.toTypedArray()
        val checkedItems = BooleanArray(tasksToShow.size)

        AlertDialog.Builder(requireContext())
            .setTitle("Import Tasks")
            .setMultiChoiceItems(taskTitles, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Import") { _, _ ->
                for (i in tasksToShow.indices) {
                    if (checkedItems[i]) {
                        val importedTask = tasksToShow[i]
                        sessionTaskAdapter.addItem(importedTask)
                        repository.addTaskToSession(importedTask.id)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        timerService?.timeLeftLiveData?.observe(viewLifecycleOwner) { millisLeft ->
            binding.timerText.text = formatTime(millisLeft)
        }

        timerService?.isFinishedLiveData?.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
                updateUI()
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}