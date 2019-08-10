package te_compa.mcoe_news_portal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class NewsAdapter extends ArrayAdapter<newsData> {

    public NewsAdapter(Context context, int resource, List<newsData> objects) {
        super(context, resource, objects);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater()
                    .inflate(R.layout.item_news, parent,
                            false);
        }
        TextView newsTitle = convertView.findViewById(R.id.newsTitleView);
        TextView dateview = convertView.findViewById(R.id.dateTextView);
        TextView introview = convertView.findViewById(R.id.introTextView);
        newsData news = getItem(position);
        if(news.getApproved()){
            convertView.setBackgroundColor(Color.argb(171,145,247,159));
        } else {
            convertView.setBackgroundColor(Color.argb(171,250,62,62));
        }
        int artLen = news.article.length();
        if(artLen > 25){
            artLen = 25;
        }
        newsTitle.setText(news.newsTitle);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("E, MMM d 'at' h:m a");
        dateview.setText(dateFormatter.format(news.date));
        introview.setText(news.article.substring(0,artLen)+"...");
        return convertView;
    }
}
