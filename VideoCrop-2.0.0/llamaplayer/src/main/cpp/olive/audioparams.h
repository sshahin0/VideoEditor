/***

  Olive - Non-Linear Video Editor
  Copyright (C) 2023 Olive Studios LLC

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.

***/

#ifndef LIBOLIVECORE_AUDIOPARAMS_H
#define LIBOLIVECORE_AUDIOPARAMS_H

extern "C" {
#include <libavutil/channel_layout.h>
}

#include <assert.h>
#include <vector>

#include "sampleformat.h"
#include "rational.h"

namespace olive::core {

class AudioParams {
public:
  AudioParams() :
    sample_rate_(0),
    channel_layout_(0),
    format_(SampleFormat::INVALID)
  {
    set_default_footage_parameters();

    // Cache channel count
    calculate_channel_count();
  }

  AudioParams(const int& sample_rate, const uint64_t& channel_layout, const SampleFormat& format) :
    sample_rate_(sample_rate),
    channel_layout_(channel_layout),
    format_(format)
  {
    set_default_footage_parameters();
    timebase_ = sample_rate_as_time_base();

    // Cache channel count
    calculate_channel_count();
  }

  int sample_rate() const
  {
    return sample_rate_;
  }

  void set_sample_rate(int sample_rate)
  {
    sample_rate_ = sample_rate;
  }

  uint64_t channel_layout() const
  {
    return channel_layout_;
  }

  void set_channel_layout(uint64_t channel_layout)
  {
    channel_layout_ = channel_layout;
    calculate_channel_count();
  }

  rational time_base() const
  {
    return timebase_;
  }

  void set_time_base(const rational& timebase)
  {
    timebase_ = timebase;
  }

  rational sample_rate_as_time_base() const
  {
    return rational(1, sample_rate());
  }

  SampleFormat format() const
  {
    return format_;
  }

  void set_format(SampleFormat format)
  {
    format_ = format;
  }

  bool enabled() const
  {
    return enabled_;
  }

  void set_enabled(bool e)
  {
    enabled_ = e;
  }

  int stream_index() const
  {
    return stream_index_;
  }

  void set_stream_index(int s)
  {
    stream_index_ = s;
  }

  int64_t duration() const
  {
    return duration_;
  }

  void set_duration(int64_t duration)
  {
    duration_ = duration;
  }

  int64_t time_to_bytes(const double& time) const;
  int64_t time_to_bytes(const rational& time) const;
  int64_t time_to_bytes_per_channel(const double& time) const;
  int64_t time_to_bytes_per_channel(const rational& time) const;
  int64_t time_to_samples(const double& time) const;
  int64_t time_to_samples(const rational& time) const;
  int64_t samples_to_bytes(const int64_t& samples) const;
  int64_t samples_to_bytes_per_channel(const int64_t& samples) const;
  rational samples_to_time(const int64_t& samples) const;
  int64_t bytes_to_samples(const int64_t &bytes) const;
  rational bytes_to_time(const int64_t &bytes) const;
  rational bytes_per_channel_to_time(const int64_t &bytes) const;
  int channel_count() const;
  int bytes_per_sample_per_channel() const;
  int bits_per_sample() const;
  bool is_valid() const;

  bool operator==(const AudioParams& other) const;
  bool operator!=(const AudioParams& other) const;

  static const std::vector<uint64_t> kSupportedChannelLayouts;
  static const std::vector<int> kSupportedSampleRates;

private:
  void set_default_footage_parameters()
  {
    enabled_ = true;
    stream_index_ = 0;
    duration_ = 0;
  }

  void calculate_channel_count();

  int sample_rate_;

  uint64_t channel_layout_;

  int channel_count_;

  SampleFormat format_;

  // Footage-specific
  int enabled_; // Switching this to int fixes GCC 11 stringop-overflow issue, I guess a byte-alignment issue?
  int stream_index_;
  int64_t duration_;
  rational timebase_;

};

}

#endif // LIBOLIVECORE_AUDIOPARAMS_H
