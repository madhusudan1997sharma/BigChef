package com.magarex.bigchef.ui.exoplayer;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.magarex.bigchef.R;
import com.magarex.bigchef.databinding.FragmentExoPlayerBinding;
import com.magarex.bigchef.model.Step;
import com.magarex.bigchef.ui.base.BaseFragment;

import java.util.List;
import java.util.Objects;

public class ExoPlayerFragment extends BaseFragment<FragmentExoPlayerBinding> {

    private Step mStep;
    private SimpleExoPlayer exoPlayer;
    private Boolean isTablet = false;

    public ExoPlayerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mStep = savedInstanceState.getParcelable(getString(R.string.steps));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (mStep != null) {
            if (!isTablet) {
                prepareView(savedInstanceState);
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    getDataBinding().exoGroup.setVisibility(View.GONE);
                    ConstraintLayout.LayoutParams exoParams = (ConstraintLayout.LayoutParams) getDataBinding().recipePlayerView.getLayoutParams();
                    exoParams.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
                    ConstraintLayout.LayoutParams errorParams = (ConstraintLayout.LayoutParams) getDataBinding().ivError.getLayoutParams();
                    errorParams.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
                }
            } else {
                prepareView(savedInstanceState);
            }

        } else Toast.makeText(getActivity(), "No Steps Available", Toast.LENGTH_SHORT).show();
    }

    private void prepareView(Bundle savedInstanceState) {
        getDataBinding().setStep(mStep);
        initializePlayer();
        getDataBinding().recipePlayerView.setPlayer(exoPlayer);
        if (savedInstanceState != null && savedInstanceState.getLong(getString(R.string.player_current_position)) != 0) {
            exoPlayer.seekTo(savedInstanceState.getLong(getString(R.string.player_current_position)));
            exoPlayer.setPlayWhenReady(savedInstanceState.getBoolean(getString(R.string.player_state)));
        }
    }

    private void initializePlayer() {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
        Uri uri = Uri.parse(mStep.getVideoURL());
        MediaSource mediaSource = buildMediaSource(uri);
        exoPlayer.prepare(mediaSource, true, false);
        exoPlayer.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource
                .Factory(new DefaultHttpDataSourceFactory(requireContext().getPackageName()))
                .createMediaSource(uri);
    }

    public void setStep(Step step) {
        this.mStep = step;
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
        }
    }

    @Override
    protected int provideLayout() {
        return R.layout.fragment_exo_player;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (exoPlayer != null && exoPlayer.getCurrentPosition() != 0) {
            outState.putLong(getString(R.string.player_current_position), exoPlayer.getCurrentPosition());
            outState.putBoolean(getString(R.string.player_state), exoPlayer.getPlayWhenReady());
        }
        outState.putParcelable(getString(R.string.steps), mStep);
    }

    public void setTablet(Boolean tablet) {
        isTablet = tablet;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        releasePlayer();
        exoPlayer = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
        exoPlayer = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer == null) {
            initializePlayer();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.getPlaybackState();
            }
            if (isVisibleToUser) {
                exoPlayer.setPlayWhenReady(true);
                exoPlayer.getPlaybackState();
            }
        }
    }
}
