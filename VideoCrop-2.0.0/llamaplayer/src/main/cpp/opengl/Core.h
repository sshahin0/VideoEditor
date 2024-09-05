//
// Created by dev-lock on ২২/২/২৪.
//

#ifndef OPENGLESNATIVE_CORE_H
#define OPENGLESNATIVE_CORE_H

#include "memory"

namespace Vivid {
    template<typename T>
    using Scope = std::unique_ptr<T>;

    template<typename T>
    using Ref = std::shared_ptr<T>;
}

#endif //OPENGLESNATIVE_CORE_H
