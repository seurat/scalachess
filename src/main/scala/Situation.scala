package chess

case class Situation(board: Board, color: Color) {

  lazy val actors = board actorsOf color

  lazy val moves: Map[Pos, List[Move]] = board.variant.validMoves(this)

  lazy val playerCanCapture: Boolean = moves exists (_._2 exists (_.captures))

  lazy val destinations: Map[Pos, List[Pos]] = moves mapValues { _ map (_.dest) }

  def drops: Option[List[Pos]] = board.variant match {
    case v@variant.Crazyhouse => v possibleDrops this
    case _                    => None
  }

  lazy val kingPos: Option[Pos] = board kingPosOf color

  lazy val check: Boolean = board check color

  def history = board.history

  def checkMate: Boolean = check && moves.isEmpty

  def staleMate: Boolean = board.variant.staleMate(this)

  def autoDraw: Boolean = board.autoDraw || board.variant.specialDraw(this)

  lazy val threefoldRepetition: Boolean = board.history.threefoldRepetition

  def variantEnd = board.variant specialEnd this

  def end: Boolean = checkMate || staleMate || autoDraw || variantEnd

  def winner: Option[Color] = board.variant.winner(this)

  def playable(strict: Boolean): Boolean =
    (board valid strict) && !end && !copy(color = !color).check

  def status: Option[Status] =
    if (checkMate) Status.Mate.some
    else if (variantEnd) Status.VariantEnd.some
    else if (staleMate) Status.Stalemate.some
    else if (autoDraw) Status.Draw.some
    else none

  def move(from: Pos, to: Pos, promotion: Option[PromotableRole]): Valid[Move] =
    board.variant.move(this, from, to, promotion)

  def drop(role: Role, pos: Pos): Valid[Drop] =
    board.variant.drop(this, role, pos)

  def withHistory(history: History) = copy(
    board = board withHistory history
  )

  def withVariant(variant: chess.variant.Variant) = copy(
    board = board withVariant variant
  )

  def canCastle = board.history.canCastle _

  def unary_! = copy(color = !color)
}

object Situation {

  def apply(variant: chess.variant.Variant): Situation = Situation(Board init variant, White)
}
