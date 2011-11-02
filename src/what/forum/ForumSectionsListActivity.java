package what.forum;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import what.gui.MyActivity;
import what.gui.R;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import api.forum.forumsections.ForumSections;
import api.forum.forumsections.Forums;

public class ForumSectionsListActivity extends MyActivity implements OnClickListener {
	private LinearLayout scrollLayout;
	private int counter;
	private ProgressDialog dialog;
	ForumSections forumSections;
	private List<Forums> forumsList;
	private ArrayList<TextView> sectionTitleList = new ArrayList<TextView>();
	private LinkedList<TextView> sectionList = new LinkedList<TextView>();
	private Intent intent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sections);
		scrollLayout = (LinearLayout) this.findViewById(R.id.scrollLayout);

		new LoadForumSections().execute();
	}

	private void populateLayout() {
		forumSections.loadForumsList();
		forumsList = forumSections.getForumsList();
		// TODO if statement
		for (int i = 0; i < forumSections.getResponse().getCategories().size(); i++) {
			sectionTitleList.add((TextView) getLayoutInflater().inflate(R.layout.forum_section_title, null));
			sectionTitleList.get(i).setText(forumSections.getResponse().getCategories().get(i).getCategoryName());
			scrollLayout.addView(sectionTitleList.get(i));
			for (int j = 0; j < forumSections.getResponse().getCategories().get(i).getForums().size(); j++) {
				if ((j % 2) == 0) {
					sectionList.add((TextView) getLayoutInflater().inflate(R.layout.forum_name_even, null));
				} else {
					sectionList.add((TextView) getLayoutInflater().inflate(R.layout.forum_name_odd, null));
				}
				sectionList.getLast().setText(
						forumSections.getResponse().getCategories().get(i).getForums().get(j).getForumName());
				sectionList.getLast().setId(j + counter);
				sectionList.getLast().setOnClickListener(this);
				scrollLayout.addView(sectionList.getLast());
				counter++;
			}
		}
	}

	private void openSection(int i) {
		Bundle b = new Bundle();
		intent = new Intent(ForumSectionsListActivity.this, what.forum.SectionActivity.class);
		b.putInt("id", Integer.valueOf(forumsList.get(i).getForumId()));
		b.putInt("page", 1);
		intent.putExtras(b);
		startActivityForResult(intent, 0);
	}

	@Override
	public void onClick(View v) {
		if ((v.getId() >= 0) && (counter >= v.getId())) {
			openSection(v.getId());
		}
	}

	private class LoadForumSections extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			lockScreenRotation();
			dialog = new ProgressDialog(ForumSectionsListActivity.this);
			dialog.setIndeterminate(true);
			dialog.setMessage("Loading...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			forumSections = ForumSections.init();
			// TODO fix
			return true;
		}

		@Override
		protected void onPostExecute(Boolean status) {

			populateLayout();
			if (status == false) {
				Toast.makeText(ForumSectionsListActivity.this, "Could not load forum sections", Toast.LENGTH_LONG).show();
			}
			dialog.dismiss();
			unlockScreenRotation();
		}
	}
}
