package what.notifications;

import java.util.ArrayList;

import what.gui.MyActivity;
import what.gui.R;
import what.services.NotificationService;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import api.notifications.Notifications;

public class NotificationsActivity extends MyActivity implements OnClickListener {
	private TextView title;
	private ArrayList<TextView> torrentList = new ArrayList<TextView>();
	private LinearLayout scrollLayout;
	private Intent intent;
	private Notifications notifications;
	private ProgressDialog dialog;
	private int page;
	private Button backButton, nextButton;
	private NotificationManager myNotificationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentLayout(R.layout.notifications);
		myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		myNotificationManager.cancel(NotificationService.ID);
		title = (TextView) this.findViewById(R.id.title);
		backButton = (Button) this.findViewById(R.id.previousButton);
		nextButton = (Button) this.findViewById(R.id.nextButton);

		getBundle();

		if (page == 1) {
			if (NotificationService.isRunning()) {
				notifications = NotificationService.notifications;
				populateLayout();
			}
			if (!NotificationService.isRunning()) {
				new LoadNotifications().execute();
			}
		} else {
			new LoadNotifications().execute();
		}

	}

	private void getBundle() {
		Bundle b = this.getIntent().getExtras();
		page = b.getInt("page");
		if ((Integer.valueOf(page) == null)) {
			page = 1;
		}
	}

	private void populateLayout() {
		backButton.setEnabled(notifications.hasPreviousPage());
		nextButton.setEnabled(notifications.hasNextPage());

		title.setText("Notifications, page " + notifications.getResponse().getCurrentPages().toString() + ", "
				+ notifications.getResponse().getNumNew() + " new notifications");
		for (int i = 0; i < notifications.getResponse().getResults().size(); i++) {
			if ((i % 2) == 0) {
				torrentList.add((TextView) getLayoutInflater().inflate(R.layout.torrent_name_even, null));
			} else {
				torrentList.add((TextView) getLayoutInflater().inflate(R.layout.torrent_name_odd, null));
			}
			if (notifications.getResponse().getResults().get(i).isUnread()) {
				torrentList.get(i).setTextColor(Color.RED);
			}
			torrentList.get(i).setText(
					notifications.getResponse().getResults().get(i).getGroupName() + " "
							+ notifications.getResponse().getResults().get(i).getYearMediaFormatEncoding());
			torrentList.get(i).setId(i);
			torrentList.get(i).setOnClickListener(this);
			scrollLayout.addView(torrentList.get(i));
		}
	}

	private void openTorrent(int i) {
		Bundle b = new Bundle();
		intent = new Intent(NotificationsActivity.this, what.torrents.torrents.TorrentTabActivity.class);
		b.putString("torrentGroupId", notifications.getResponse().getResults().get(i).getGroupId());
		intent.putExtras(b);
		startActivityForResult(intent, 0);
	}

	public void clear(View v) {
		notifications.clearNotifications();
		torrentList.clear();
		scrollLayout.removeAllViews();
		backButton.setEnabled(false);
		nextButton.setEnabled(false);

	}

	public void next(View v) {
		Bundle b = new Bundle();
		intent = new Intent(NotificationsActivity.this, what.notifications.NotificationsActivity.class);
		b.putInt("page", page + 1);
		intent.putExtras(b);
		startActivityForResult(intent, 0);
	}

	public void back(View v) {
		Bundle b = new Bundle();
		intent = new Intent(NotificationsActivity.this, what.notifications.NotificationsActivity.class);
		b.putInt("page", page - 1);
		intent.putExtras(b);
		startActivityForResult(intent, 0);
	}

	@Override
	public void onClick(View v) {
		for (int i = 0; i < (torrentList.size()); i++) {
			if (v.getId() == torrentList.get(i).getId()) {
				openTorrent(i);
			}
		}
	}

	private class LoadNotifications extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			lockScreenRotation();
			dialog = new ProgressDialog(NotificationsActivity.this);
			dialog.setIndeterminate(true);
			dialog.setMessage("Loading...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			notifications = Notifications.notificationsFromPage(page);
			return notifications.getStatus();
		}

		@Override
		protected void onPostExecute(Boolean status) {
			populateLayout();
			if (status == false) {
				Toast.makeText(NotificationsActivity.this, "Could not load notifications", Toast.LENGTH_LONG).show();
			}
			dialog.dismiss();
			unlockScreenRotation();
		}
	}

}
