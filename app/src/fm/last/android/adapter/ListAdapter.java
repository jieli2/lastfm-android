package fm.last.android.adapter;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.AbsListView.OnScrollListener;

import fm.last.android.R;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.ImageDownloader;
import fm.last.android.utils.ImageDownloaderListener;

/**
 * Simple adapter for presenting ArrayList of IconifiedEntries as ListView, 
 * allows icon customization
 * 
 * @author Lukasz Wisniewski
 */
public class ListAdapter extends BaseAdapter implements ImageDownloaderListener, OnScrollListener {
	
	protected ImageCache mImageCache;
	protected ImageDownloader mImageDownloader;
	protected Activity mContext;
	
	private ArrayList<ListEntry> mList;
	private boolean mScrolling = false;
	private int mLoadingBar = -1;
	
	public ListAdapter(Activity context) {
		mContext = context;
	}
	
	/**
	 * Default constructor
	 * 
	 * @param context
	 * @param imageCache
	 */
	public ListAdapter(Activity context, ImageCache imageCache) {
		mContext = context;
		init(imageCache);
	}

	/**
	 * Sharable code between constructors
	 * 
	 * @param imageCache
	 */
	private void init(ImageCache imageCache){
		mImageDownloader = new ImageDownloader(imageCache);
		mImageDownloader.setListener(this);
		mImageCache = imageCache;
		mList = new ArrayList<ListEntry>();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;

		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.list_row, null);

			holder = new ViewHolder();
			holder.label = (TextView)row.findViewById(R.id.row_label);
			holder.image = (ImageView)row.findViewById(R.id.row_icon);
			holder.disclosure = (ImageView)row.findViewById(R.id.row_disclosure_icon);
			holder.vs = (ViewSwitcher)row.findViewById(R.id.row_view_switcher);

			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}

		holder.label.setText(mList.get(position).text);
		if(mList.get(position).icon_id == -1)
			holder.image.setVisibility(View.GONE);
		else
			holder.image.setVisibility(View.VISIBLE);

		// set disclosure image (if set)
		if(mList.get(position).disclosure_id != -1){
			holder.vs.setVisibility(View.VISIBLE);
			holder.disclosure.setImageResource(mList.get(position).disclosure_id);
		} else {
			holder.vs.setVisibility(View.GONE);
		}
		
		if(mLoadingBar == position) {
			holder.vs.setDisplayedChild(1);
			holder.label.setTextColor(0xFFFFFFFF);
	    	row.setBackgroundResource(R.drawable.list_item_focus);
		}
		else{
			holder.vs.setDisplayedChild(0);
			holder.label.setTextColor(0xFF000000);
	    	row.setBackgroundResource(R.drawable.list_item_rest);
		}

		// optionally if an URL is specified
		if(mList.get(position).url != null){
			Bitmap bmp = mImageCache.get(mList.get(position).url);
			if(bmp != null){
				holder.image.setImageBitmap(bmp);
			} else {
				holder.image.setImageResource(mList.get(position).icon_id);
			}
		} else {
			holder.image.setImageResource(mList.get(position).icon_id);
		}

		return row;
	}

	/**
	 * Holder pattern implementation,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		TextView label;
		ImageView image;
		ImageView disclosure;
		ViewSwitcher vs;
	}

	/**
	 * Sets list of Iconified Entires as a source for the adapter
	 * 
	 * @param list
	 */
	public void setSourceIconified(ArrayList<ListEntry> list) {
		mList = list;

		ArrayList<String> urls = new ArrayList<String>();
		Iterator<ListEntry> it = list.iterator ();
		while (it.hasNext ()) {
			ListEntry entry = it.next();
			if(entry.url != null){
				urls.add(entry.url);
			}
		}

//		super.setSource(oldList);

		if(mImageDownloader.getUserTask() == null){
			mImageDownloader.getImages(urls);
		}
	}

	public void asynOperationEnded() {
		this.notifyDataSetChanged();
		
//		if(mListener != null){
//			mListener.end();
//		}
	}

	public void imageDownloadProgress(int imageDownloaded, int imageCount) {
		// TODO We need to re-work this so the imageviews get updated instead of
		// refreshing the entire list.  That way the selection indicator will
		// not be reset to the top whenever a new item loads
		
		if(!mScrolling){
			this.notifyDataSetInvalidated();
		} else {
			this.notifyDataSetChanged();
		}
	}

	public void asynOperationStarted() {
		// TODO mDownloading = true;
//		if(mListener != null){
//			mListener.started();
//		}
	}

//	public void setListener(PreparationListener mListener) {
//		this.mListener = mListener;
//	}
	
	public OnScrollListener getOnScrollListener(){
		return this;
	}
	
	/**
	 * Enables load bar at given position,
	 * at the same time only one can
	 * be launched per adapter
	 * 
	 * @param position
	 */
	public void enableLoadBar(int position){
		this.mLoadingBar = position;
		notifyDataSetChanged();
	}
	
	/**
	 * Disables load bar
	 */
	public void disableLoadBar(){
		this.mLoadingBar = -1;
		notifyDataSetChanged();
	}


	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}


	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrolling = true;
	}

	public int getCount() {
		if(mList != null)
			return mList.size();
		else
			return 0;
	}

	public Object getItem(int position) {
		return mList.get(position).value;
	}

	public long getItemId(int position) {
		return position;
	}

}
