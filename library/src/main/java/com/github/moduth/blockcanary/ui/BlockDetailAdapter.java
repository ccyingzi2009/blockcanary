package com.github.moduth.blockcanary.ui;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.github.moduth.blockcanary.R;
import com.github.moduth.blockcanary.log.Block;
import com.github.moduth.blockcanary.log.ProcessUtils;

import java.util.Arrays;

/**
 * @author yifan.zhai on 15/9/27.
 */
final class BlockDetailAdapter extends BaseAdapter {

    private static final int TOP_ROW = 0;
    private static final int NORMAL_ROW = 1;

    private boolean[] mFoldings = new boolean[0];

    private Block mBlock;

    private static final int POSITION_BASIC = 1;
    private static final int POSITION_TIME = 2;
    private static final int POSITION_CPU = 3;
    private static final int POSITION_THREAD_STACK = 4;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        if (getItemViewType(position) == TOP_ROW) {
            if (convertView == null) {
                convertView =
                        LayoutInflater.from(context).inflate(R.layout.block_canary_ref_top_row, parent, false);
            }
            TextView textView = findById(convertView, R.id.__leak_canary_row_text);
            textView.setText(context.getPackageName());
        } else {
            if (convertView == null) {
                convertView =
                        LayoutInflater.from(context).inflate(R.layout.block_canary_ref_row, parent, false);
            }
            TextView textView = findById(convertView, R.id.__leak_canary_row_text);

            boolean isThreadStackEntry = position == POSITION_THREAD_STACK + 1;
            String element = getItem(position);
            String htmlString = elementToHtmlString(element, position, mFoldings[position]);
            if (isThreadStackEntry && !mFoldings[position]) {
                htmlString += " <font color='#919191'>" + "blocked" + "</font>";
            }
            textView.setText(Html.fromHtml(htmlString));

            DisplayLeakConnectorView connectorView = findById(convertView, R.id.__leak_canary_row_connector);
            connectorView.setType(connectorViewType(position));

            MoreDetailsView moreDetailsView = findById(convertView, R.id.__leak_canary_row_more);
            moreDetailsView.setFolding(mFoldings[position]);
        }

        return convertView;
    }

    private DisplayLeakConnectorView.Type connectorViewType(int position) {
        return (position == 1) ? DisplayLeakConnectorView.Type.START : (
                (position == getCount() - 1) ? DisplayLeakConnectorView.Type.END :
                        DisplayLeakConnectorView.Type.NODE);
    }

    private String elementToHtmlString(String element, int position, boolean folding) {
        String htmlString = element.replaceAll(Block.SEPARATOR, "<br>");

        switch (position) {
            case POSITION_BASIC:
                if (folding) {
                    htmlString = htmlString.substring(htmlString.indexOf(Block.KEY_CPU_CORE));
                }
                htmlString = String.format("<font color='#c48a47'>%s</font> ", htmlString);
                break;
            case POSITION_TIME:
                if (folding) {
                    htmlString = htmlString.substring(0, htmlString.indexOf(Block.KEY_TIME_COST_START));
                }
                htmlString = String.format("<font color='#f3cf83'>%s</font> ", htmlString);
                break;
            case POSITION_CPU:
                // FIXME 不知道为啥有时候前面的\r\n没法完整替换
                htmlString = element;
                if (folding) {
                    htmlString = htmlString.substring(0, htmlString.indexOf(Block.KEY_CPU_RATE));
                }
                htmlString = htmlString.replace("cpurate = ", "<br>cpurate<br/>");
                htmlString = String.format("<font color='#998bb5'>%s</font> ", htmlString);
                htmlString = htmlString.replaceAll("]", "]<br>");
                break;
            case POSITION_THREAD_STACK:
            default:
                if (folding) {
                    int index = htmlString.indexOf(ProcessUtils.myProcessName());
                    if (index > 0) {
                        htmlString = htmlString.substring(index);
                    }
                }
                htmlString = String.format("<font color='#ffffff'>%s</font> ", htmlString);
                break;
        }
        return htmlString;
    }

    public void update(Block block) {
        if (mBlock != null && block.timeStart.equals(mBlock.timeStart)) {
            // Same data, nothing to change.
            return;
        }
        this.mBlock = block;
        mFoldings = new boolean[POSITION_THREAD_STACK + mBlock.threadStackEntries.size()];
        Arrays.fill(mFoldings, true);
        notifyDataSetChanged();
    }

    public void toggleRow(int position) {
        mFoldings[position] = !mFoldings[position];
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mBlock == null) {
            return 0;
        }
        return POSITION_THREAD_STACK + mBlock.threadStackEntries.size();
    }

    @Override
    public String getItem(int position) {
        if (getItemViewType(position) == TOP_ROW) {
            return null;
        }
        switch (position) {
            case POSITION_BASIC:
                return mBlock.getBasicString();
            case POSITION_TIME:
                return mBlock.getTimeString();
            case POSITION_CPU:
                return mBlock.getCpuString();
            case POSITION_THREAD_STACK:
            default:
                return mBlock.threadStackEntries.get(position - POSITION_THREAD_STACK);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TOP_ROW;
        }
        return NORMAL_ROW;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("unchecked")
    private static <T extends View> T findById(View view, int id) {
        return (T) view.findViewById(id);
    }
}

