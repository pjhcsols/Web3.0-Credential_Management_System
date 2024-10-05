//
//  SwifeItemView.swift
//  Wallet
//
//  Created by Seah Kim on 10/5/24.
//

import SwiftUI

struct SwipeItemView<Content: View, Right: View>: View {
    var content: () -> Content
    var right: () -> Right
    var itemHeight: CGFloat
    
    init(content: @escaping () -> Content, right: @escaping () -> Right, itemHeight: CGFloat) {
        self.content = content
        self.right = right
        self.itemHeight = itemHeight
    }
    
    @State var hoffset: CGFloat = 0
    @State var anchor: CGFloat = 0
    
    let screenWidth = UIScreen.main.bounds.width
    var anchorWidth: CGFloat { screenWidth / 3 }
    var swipeTreshold: CGFloat { screenWidth / 15 }
    
    @State var rightPast = false
    
    var drag: some Gesture {
        DragGesture()
            .onChanged { value in
                withAnimation {
                    hoffset = anchor + value.translation.width
                    
                    if abs(hoffset) > anchorWidth {
                        if rightPast {
                            hoffset = -anchorWidth
                        }
                    }
                    
                    if anchor < 0 {
                        rightPast = hoffset < -anchorWidth + swipeTreshold
                    } else {
                        rightPast = hoffset < -swipeTreshold
                    }
                }
            }
            .onEnded { value in
                withAnimation {
                    if rightPast {
                        anchor = -anchorWidth
                    } else {
                        anchor = 0
                    }
                    hoffset = anchor
                }
            }
    }
    
    var body: some View {
        GeometryReader { geo in
            HStack(spacing: 0) {
                content()
                    .frame(width: geo.size.width + 20)
                right()
                    .frame(width: anchorWidth)
                    .zIndex(1)
                    .clipped()
            }
            .offset(x: hoffset)
            .frame(maxHeight: 76)
            .contentShape(Rectangle())
            .gesture(drag)
            .clipped()
        }
    }
}

#Preview {
    SwifeItemView()
}
