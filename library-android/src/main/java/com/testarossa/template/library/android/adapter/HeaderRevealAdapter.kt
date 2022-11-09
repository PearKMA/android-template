package com.testarossa.template.library.android.adapter

/**
 * Base header with swipeLayout recycler
 */
/*
class HeaderRevealAdapter (
    diffCallback: DiffUtil.ItemCallback<T>,
    private val inflater: LayoutInflater
) : ListAdapter<T, HeaderRevealAdapter.ViewHolderBase<T>>(diffCallback) {
    // region Const and Fields
    var listener: SwipeAdapterListener? = null
    private val viewBinderHelper = ViewBinderHelper()

    // endregion

    // region override method
    override fun getItemViewType(position: Int): Int {
        return getItem(position).itemType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBase<T> {
        return when (viewType) {
            AppLayout.item_history -> ViewHolderBase(
                ItemHistoryBinding.inflate(inflater, parent, false)
            )
            AppLayout.item_header_history -> ViewHolderBase(
                ItemHeaderHistoryBinding.inflate(inflater, parent, false)
            )
            else -> ViewHolderBase(
                ItemHistoryBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolderBase<T>, position: Int) {
        val item = getItem(position)
        if (getItemViewType(position) == AppLayout.item_history && null != holder.swipeLayout) {
            viewBinderHelper.openOnlyOne = true
            viewBinderHelper.bind(holder.swipeLayout, item.id.toString())
            viewBinderHelper.closeLayout(item.id.toString())
            if (item.multiSelect) {
                viewBinderHelper.lockSwipe(item.id.toString())
            } else {
                viewBinderHelper.unlockSwipe(item.id.toString())
            }
        }
        holder.bind(item, listener)
    }

    fun closeSwipe(model: T) {
        viewBinderHelper.closeLayout(model.id.toString())
    }
    // endregion

    // region ViewHolder
    class ViewHolderBase<T : BaseModel>(
        private val binding: ViewDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val swipeLayout = if (binding is ItemHistoryBinding) binding.swipeLayout else null

        fun bind(data: T, diffListener: SwipeAdapterListener?) {
            binding.apply {
                setVariable(BR.item, data)
                setVariable(BR.listener, diffListener)
                executePendingBindings()
            }
        }
    }
    // endregion

    // region listener
    interface SwipeAdapterListener {
        fun onShare(stone: T)
        fun onDelete(stone: T)
        fun onSearchStone(isClosed: Boolean, stone: T)
    }
    // endregion
}*/
