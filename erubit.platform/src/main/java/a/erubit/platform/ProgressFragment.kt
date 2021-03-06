package a.erubit.platform

import a.erubit.platform.LessonsFragment.OnLessonInteractionListener
import a.erubit.platform.course.*
import a.erubit.platform.course.CourseManager
import a.erubit.platform.course.lesson.BunchLesson
import a.erubit.platform.course.lesson.Lesson

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import u.U

import java.util.*


class ProgressFragment : Fragment() {
	private var mListener: OnLessonInteractionListener? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_progress, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val listView: RecyclerView = view.findViewById(android.R.id.list)
		listView.itemAnimator = DefaultItemAnimator()
		listView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

		val course = CourseManager.i().getCourse(arguments!!.getString("id"))
		(view.findViewById<View>(R.id.courseName) as TextView).text = course!!.name
		listView.adapter = ProgressListAdapter(course)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

		if (context is OnLessonInteractionListener)
			mListener = context
	}


	private inner class ProgressListAdapter internal constructor(course: Course?) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
		private val mList: MutableList<ListItem>

		override fun getItemViewType(position: Int): Int {
			return mList[position].type
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
			val inflater = LayoutInflater.from(parent.context)

			return when (viewType) {
				HEADER -> {
					val view = inflater.inflate(R.layout.item_progress_header, parent, false)
					HeaderViewHolder(view)
				}
				CONTENT -> {
					val view = inflater.inflate(R.layout.item_progress_content, parent, false)
					ContentViewHolder(view)
				}
				else -> throw UnsupportedOperationException("ViewType value `$viewType` is not supported.")
			}
		}

		override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
			if (holder is HeaderViewHolder) {
				holder.mLesson = (mList[position] as HeaderItem).mLesson
				holder.textTitle.text = (mList[position] as HeaderItem).mName
			}

			if (holder is ContentViewHolder) {
				val item = mList[position] as ContentItem
				holder.textTitle.text = U.defurigana(item.mText)
				holder.textKnowledge.text = item.mKnowledge
			}
		}

		override fun getItemCount(): Int {
			return mList.size
		}


		private inner class HeaderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
			var mLesson: Lesson? = null
			val textTitle: TextView = itemView.findViewById(android.R.id.text1)
            private val btnPractice: Button = itemView.findViewById(android.R.id.button1)

            init {
                btnPractice.setOnClickListener { mListener!!.onLessonInteraction(mLesson, LessonsFragment.LessonInteractionAction.PRACTICE) }
			}
		}

		private inner class ContentViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
			val textTitle: TextView = itemView.findViewById(android.R.id.text1)
            val textKnowledge: TextView = itemView.findViewById(android.R.id.text2)
        }


        internal abstract inner class ListItem {
			abstract val type: Int
		}


		private inner class HeaderItem internal constructor(val mLesson: Lesson) : ListItem() {
			val mName: String? = mLesson.name
			override val type: Int
				get() = HEADER

		}


		private inner class ContentItem internal constructor(val mText: String, val mKnowledge: String) : ListItem() {
			override val type: Int
				get() = CONTENT
		}


		init {
			mList = ArrayList(10)
            val ctx = context!!

			for (lesson in course!!.lessons.values) {
				mList.add(HeaderItem(lesson))

				if (lesson is BunchLesson) {
					for (item in lesson.mSet!!)
						mList.add(ContentItem(item.title, lesson.getKnowledgeText(ctx, item.knowledgeLevel)))
				} else {
					mList.add(ContentItem(lesson.getProgress(ctx).getExplanation(ctx), ""))
				}
			}
		}
	}


	companion object {
		const val HEADER = 0
		const val CONTENT = 1
	}

}
