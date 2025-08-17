package com.example.training_weaver.UI

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.training_weaver.ViewModels.ExerciseDatabaseViewModel
import com.example.training_weaver.dataclass.Exercise
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.training_weaver.databinding.FragmentExercisePlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Locale
import java.util.regex.Pattern

class ExercisePlayerFragment : Fragment() {

    private var _binding: FragmentExercisePlayerBinding? = null
    private val b get() = _binding!!

    private val args: ExercisePlayerFragmentArgs by navArgs()
    private val vmExercises: ExerciseDatabaseViewModel by activityViewModels()

    private var youTubeId: String? = null
    private var youTubePlayer: YouTubePlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExercisePlayerBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // תצמיד את נגן היוטיוב למחזור חיי הפרגמנט
        lifecycle.addObserver(b.youtubeView)

        val args = ExercisePlayerFragmentArgs.fromBundle(requireArguments())
        val m = args.meta
        val ex = args.exercise
        b.tvName.text = ex.exerciseName
        // טקסטים של המופע ברוטינה
        b.tvSets.text = "Sets: ${m?.sets ?: "-"}"
        b.tvReps.text = "Reps: ${m?.reps ?: "-"}"
        b.tvRest.text = "Rest: ${m?.restTimeSeconds ?: "-"} s"

        // נביא את ה-Exercise ונקשר אותו למסך
        vmExercises.exercises.observe(viewLifecycleOwner) { list ->
            val ex = list.firstOrNull { it.exerciseID == m?.exerciseID }
            if (ex != null) bindExercise(ex) else vmExercises.load()
        }
        vmExercises.load()

        // הכנת הנגן – נרשמים ל-onReady כדי לשמור רפרנס ל-YouTubePlayer
        b.youtubeView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
                // לא מפעילים אוטומטית; נחכה ללחיצה על פליי
                youTubeId?.let { id -> player.cueVideo(id, 0f) }
            }
        })
    }

    private fun bindExercise(ex: Exercise) {
        b.tvName.text = ex.exerciseName
        b.tvDescription.text = ex.exerciseDescription.orEmpty()

        val url = ex.url.orEmpty()
        youTubeId = extractYoutubeId(url)

        // אם יש מזהה יוטיוב → נטען thumbnail ונאפשר פליי בנגן המוטמע
        if (!youTubeId.isNullOrBlank()) {
            val thumbUrl = "https://img.youtube.com/vi/$youTubeId/hqdefault.jpg"
            loadImageIntoView(thumbUrl)

            val play = {
                // מסתירים thumbnail ומראים את הנגן
                b.thumbContainer.visibility = View.GONE
                b.youtubeView.visibility = View.VISIBLE
                youTubeId?.let { id -> youTubePlayer?.loadVideo(id, 0f) }
            }
            b.thumbContainer.setOnClickListener { play() }
            b.btnPlay.setOnClickListener { play() }
        } else {
            // לא יוטיוב – נפתח באפליקציה/דפדפן חיצוני
            b.youtubeView.visibility = View.GONE
            val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            b.thumbContainer.setOnClickListener { startActivity(openIntent) }
            b.btnPlay.setOnClickListener { startActivity(openIntent) }
        }
    }

    private fun loadImageIntoView(url: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                URL(url).openStream().use { BitmapFactory.decodeStream(it) }
            }.onSuccess { bmp ->
                withContext(Dispatchers.Main) {
                    if (isAdded) b.ivThumb.setImageBitmap(bmp)
                }
            }
        }
    }

    private fun extractYoutubeId(url: String): String? {
        if (url.isBlank()) return null
        val patterns = arrayOf(
            "v=([a-zA-Z0-9_-]{6,})",
            "youtu\\.be/([a-zA-Z0-9_-]{6,})",
            "youtube\\.com/shorts/([a-zA-Z0-9_-]{6,})"
        )
        for (p in patterns) {
            val m = Pattern.compile(p).matcher(url)
            if (m.find()) return m.group(1)
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // שחרור משאבים של הנגן
        b.youtubeView.release()
        _binding = null
    }
}
