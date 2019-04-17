package com.platform.assets.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.platform.assets.Asset;
import com.platform.assets.Utils;
import com.ravencoin.R;
import com.ravencoin.presenter.fragments.FragmentIssueUniqueAsset;
import com.ravencoin.tools.animation.BRAnimator;
import com.ravencoin.tools.util.BRConstants;
import com.ravencoin.wallet.WalletsMaster;
import com.ravencoin.wallet.abstracts.BaseWalletManager;

import java.math.BigDecimal;
import java.util.List;

import static com.platform.assets.AssetsValidation.SUB_NAME_DELIMITER;
import static com.platform.assets.AssetsValidation.UNIQUE_TAG_DELIMITER;


public class AssetsAdapter extends RecyclerView.Adapter<AssetsAdapter.ViewHolder> {

    private Context context;
    private List<Asset> assets;
    private BaseWalletManager wallet;

    public AssetsAdapter(Context context, List<Asset> assets) {
        this.context = context;
        this.assets = assets;
        this.wallet = WalletsMaster.getInstance(context).getCurrentWallet(context);
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Asset asset = assets.get(position);
        String name = asset.getName();
        if (!name.contains(SUB_NAME_DELIMITER) && !name.contains(UNIQUE_TAG_DELIMITER)) {
            holder.assetName.setText(formatAssetName(name));
            holder.rootAssetName.setVisibility(View.GONE);
        } else {
            holder.rootAssetName.setVisibility(View.VISIBLE);
            if (name.contains(UNIQUE_TAG_DELIMITER)) {
                String[] names = name.split(UNIQUE_TAG_DELIMITER);
                String subName = names[names.length - 1];
                String rootName = replaceLast(name, subName, "");
                holder.assetName.setText(formatAssetName(subName));
                holder.rootAssetName.setText(rootName);
            } else {
                String[] names = name.split(SUB_NAME_DELIMITER);
                String subName = names[names.length - 1];
                String rootName = replaceLast(name, subName, "");
                holder.assetName.setText(formatAssetName(subName));
                holder.rootAssetName.setText(rootName);
            }
        }
        double assetAmount = wallet.getCryptoForSmallestCrypto(context, new BigDecimal(asset.getAmount())).doubleValue();
        holder.assetAmount.setText(Utils.formatAssetAmount(assetAmount, asset.getUnits()));

        if (asset.getOwnership() == 1) {
            holder.viewContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_owned_asset));
            holder.ownershipImage.setVisibility(View.VISIBLE);
        } else {
            holder.viewContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_non_owned_asset));
            holder.ownershipImage.setVisibility(View.GONE);
        }
        holder.viewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BRAnimator.showAssetMenuFragment((Activity) context, asset);
            }
        });
    }

    private String formatAssetName(String name) {
        if (name == null)
            return "";
        else if (name.length() <= 10)
            return name;
        else return name.substring(0, 5) + "..." + name.substring(name.length() - 2);
    }

    @Override
    public int getItemCount() {
        return assets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout viewContainer;
        ImageView ownershipImage;
        TextView assetName;
        TextView rootAssetName;
        TextView assetAmount;

        ViewHolder(View itemView) {
            super(itemView);
            viewContainer = itemView.findViewById(R.id.view_container);
            ownershipImage = itemView.findViewById(R.id.asset_ownership_image);
            assetName = itemView.findViewById(R.id.asset_name);
            rootAssetName = itemView.findViewById(R.id.root_asset_name);
            assetAmount = itemView.findViewById(R.id.asset_amount);
        }
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
}
