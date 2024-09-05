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

#ifndef LIBOLIVECORE_COLOR_H
#define LIBOLIVECORE_COLOR_H

#include "pixelformat.h"

namespace olive::core {

/**
 * @brief High precision 32-bit DataType based RGBA color value
 */
class Color
{
public:
  using DataType = float;
  static constexpr unsigned int RGBA = 4;

  Color()
  {
    for (unsigned int i=0;i<RGBA;i++) {
      data_[i] = 0.0;
    }
  }

  Color(const DataType& r, const DataType& g, const DataType& b, const DataType& a = 1.0f)
  {
    data_[0] = r;
    data_[1] = g;
    data_[2] = b;
    data_[3] = a;
  }

  Color(const char *data, const PixelFormat &format, int ch_layout);

  /**
   * @brief Creates a Color struct from hue/saturation/value
   *
   * Hue expects a value between 0.0 and 360.0. Saturation and Value expect a value between 0.0 and 1.0.
   */
  static Color fromHsv(const DataType& h, const DataType& s, const DataType &v);

  const DataType& red() const {return data_[0];}
  const DataType& green() const {return data_[1];}
  const DataType& blue() const {return data_[2];}
  const DataType& alpha() const {return data_[3];}

  void toHsv(DataType* hue, DataType* sat, DataType* val) const;
  DataType hsv_hue() const;
  DataType hsv_saturation() const;
  DataType value() const;

  void toHsl(DataType* hue, DataType* sat, DataType* lightness) const;
  DataType hsl_hue() const;
  DataType hsl_saturation() const;
  DataType lightness() const;

  void set_red(const DataType& red) {data_[0] = red;}
  void set_green(const DataType& green) {data_[1] = green;}
  void set_blue(const DataType& blue) {data_[2] = blue;}
  void set_alpha(const DataType& alpha) {data_[3] = alpha;}

  DataType* data() {return data_;}
  const DataType* data() const {return data_;}

  void toData(char *out, const PixelFormat &format, unsigned int nb_channels) const;

  static Color fromData(const char* in, const PixelFormat& format, unsigned int nb_channels);

  // Suuuuper rough luminance value mostly used for UI (determining whether to overlay with black
  // or white text)
  DataType GetRoughLuminance() const;

  // Assignment math operators
  Color& operator+=(const Color& rhs);
  Color& operator-=(const Color& rhs);
  Color& operator+=(const DataType& rhs);
  Color& operator-=(const DataType& rhs);
  Color& operator*=(const DataType& rhs);
  Color& operator/=(const DataType& rhs);

  // Binary math operators
  Color operator+(const Color& rhs) const
  {
    Color c(*this);
    c += rhs;
    return c;
  }

  Color operator-(const Color& rhs) const
  {
    Color c(*this);
    c -= rhs;
    return c;
  }

  Color operator+(const DataType& rhs) const
  {
    Color c(*this);
    c += rhs;
    return c;
  }

  Color operator-(const DataType& rhs) const
  {
    Color c(*this);
    c -= rhs;
    return c;
  }

  Color operator*(const DataType& rhs) const
  {
    Color c(*this);
    c *= rhs;
    return c;
  }

  Color operator/(const DataType& rhs) const
  {
    Color c(*this);
    c /= rhs;
    return c;
  }

private:
  DataType data_[RGBA];

};

}

#endif // LIBOLIVECORE_COLOR_H
