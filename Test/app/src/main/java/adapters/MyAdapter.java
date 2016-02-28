package adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import helpers.CellData;
import com.example.anna.test.R;

/**
 * Created by Anna on 2/19/16.
 */

public class MyAdapter extends RecyclerView.Adapter {
    private CellData[] data = new CellData[]{new CellData("t1", "c1"), new CellData("t2", "c2")};

    public MyAdapter(CellData[] data){
        this.data=data;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private View root;
        private TextView tvTitle, tvContent;


        public ViewHolder(View root) {
            super(root);
            tvTitle = (TextView) root.findViewById(R.id.tvTitle);
            tvContent = (TextView) root.findViewById(R.id.tvContent);
        }

        public TextView getTvTitle() {
            return tvTitle;
        }

        public TextView getTvContent() {
            return tvContent;
        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_cell, null));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder) holder;
        CellData cd = data[position];
        vh.getTvTitle().setText(cd.title);
        vh.getTvContent().setText(cd.content);
    }

    @Override
    public int getItemCount() {
        return data.length;
    }
}
